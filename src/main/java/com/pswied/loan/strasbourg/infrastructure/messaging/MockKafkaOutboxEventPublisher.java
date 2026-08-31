package com.pswied.loan.strasbourg.infrastructure.messaging;

import com.pswied.loan.strasbourg.application.outbox.OutboxEventPublisherPort;
import com.pswied.loan.strasbourg.domain.outbox.OutboxEvent;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class MockKafkaOutboxEventPublisher implements OutboxEventPublisherPort {

    private final Set<String> publishedEventIds = ConcurrentHashMap.newKeySet();
    private final CopyOnWriteArrayList<OutboxEvent> publishedEvents = new CopyOnWriteArrayList<>();

    @Override
    public void publish(OutboxEvent event) {
        if (shouldFail(event)) {
            throw new IllegalStateException("Mock Kafka publish failure");
        }

        if (!publishedEventIds.add(event.eventId())) {
            return;
        }

        publishedEvents.add(event);
    }

    public int publishedCount() {
        return publishedEvents.size();
    }

    public boolean hasPublished(String eventId) {
        return publishedEventIds.contains(eventId);
    }

    public List<OutboxEvent> publishedEvents() {
        return List.copyOf(publishedEvents);
    }

    private boolean shouldFail(OutboxEvent event) {
        return event.aggregateId().toLowerCase(Locale.ROOT).contains("fail-publish")
                || event.payload().contains("\"simulateFailure\":true");
    }
}
