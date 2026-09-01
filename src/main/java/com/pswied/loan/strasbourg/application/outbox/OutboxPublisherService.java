package com.pswied.loan.strasbourg.application.outbox;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;

@ApplicationScoped
public class OutboxPublisherService {

    private final OutboxEventStorePort outboxEventStorePort;
    private final OutboxEventPublisherPort outboxEventPublisherPort;

    @Inject
    public OutboxPublisherService(
            OutboxEventStorePort outboxEventStorePort,
            OutboxEventPublisherPort outboxEventPublisherPort
    ) {
        this.outboxEventStorePort = outboxEventStorePort;
        this.outboxEventPublisherPort = outboxEventPublisherPort;
    }

    public int publishPendingEvents() {
        int publishedCount = 0;
        for (var event : outboxEventStorePort.findPendingEvents()) {
            try {
                outboxEventPublisherPort.publish(event);
                outboxEventStorePort.updateEvent(event.markPublished(Instant.now()));
                publishedCount++;
            } catch (RuntimeException exception) {
                outboxEventStorePort.updateEvent(event.markRetry(exception.getMessage(), Instant.now()));
            }
        }
        return publishedCount;
    }
}
