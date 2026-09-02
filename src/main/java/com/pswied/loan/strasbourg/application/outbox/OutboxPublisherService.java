package com.pswied.loan.strasbourg.application.outbox;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.time.Instant;

@ApplicationScoped
public class OutboxPublisherService {

    private final OutboxEventStorePort outboxEventStorePort;
    private final OutboxEventPublisherPort outboxEventPublisherPort;
    private final int maxRetries;
    private final Duration retryBackoff;

    @Inject
    public OutboxPublisherService(
            OutboxEventStorePort outboxEventStorePort,
            OutboxEventPublisherPort outboxEventPublisherPort,
            @ConfigProperty(name = "strasbourg.outbox.max-retries", defaultValue = "5") int maxRetries,
            @ConfigProperty(name = "strasbourg.outbox.retry-backoff-seconds", defaultValue = "30") long retryBackoffSeconds
    ) {
        this.outboxEventStorePort = outboxEventStorePort;
        this.outboxEventPublisherPort = outboxEventPublisherPort;
        this.maxRetries = maxRetries;
        this.retryBackoff = Duration.ofSeconds(retryBackoffSeconds);
    }

    @Scheduled(every = "${strasbourg.outbox.poll-interval:5s}")
    void publishScheduledEvents() {
        publishPendingEvents();
    }

    public int publishPendingEvents() {
        int publishedCount = 0;
        Instant now = Instant.now();

        for (var event : outboxEventStorePort.findPendingEvents()) {
            if (event.retryCount() >= maxRetries) {
                outboxEventStorePort.updateEvent(event.markDeadLetter(event.lastError() != null ? event.lastError() : "Exceeded retry limit", now));
                continue;
            }

            if (event.lastRetriedAt() != null && now.isBefore(event.lastRetriedAt().plus(retryBackoff))) {
                continue;
            }

            try {
                outboxEventPublisherPort.publish(event);
                outboxEventStorePort.updateEvent(event.markPublished(now));
                publishedCount++;
            } catch (RuntimeException exception) {
                var retriedEvent = event.markRetry(exception.getMessage(), now);
                if (retriedEvent.retryCount() >= maxRetries) {
                    outboxEventStorePort.updateEvent(retriedEvent.markDeadLetter(exception.getMessage(), now));
                } else {
                    outboxEventStorePort.updateEvent(retriedEvent);
                }
            }
        }
        return publishedCount;
    }
}
