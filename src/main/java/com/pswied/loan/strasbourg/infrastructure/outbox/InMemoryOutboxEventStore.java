package com.pswied.loan.strasbourg.infrastructure.outbox;

import com.pswied.loan.strasbourg.application.outbox.OutboxEventStorePort;
import com.pswied.loan.strasbourg.domain.outbox.OutboxEvent;
import com.pswied.loan.strasbourg.domain.outbox.OutboxEventStatus;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class InMemoryOutboxEventStore implements OutboxEventStorePort {

    private final ConcurrentHashMap<String, OutboxEvent> eventsById = new ConcurrentHashMap<>();

    @Override
    public void saveEvent(OutboxEvent event) {
        eventsById.put(event.eventId(), event);
    }

    @Override
    public void updateEvent(OutboxEvent event) {
        eventsById.put(event.eventId(), event);
    }

    @Override
    public List<OutboxEvent> findPendingEvents() {
        return eventsById.values().stream()
                .filter(event -> event.status() == OutboxEventStatus.PENDING)
                .sorted(Comparator.comparing(OutboxEvent::createdAt))
                .toList();
    }

    @Override
    public Optional<OutboxEvent> findByEventId(String eventId) {
        return Optional.ofNullable(eventsById.get(eventId));
    }
}
