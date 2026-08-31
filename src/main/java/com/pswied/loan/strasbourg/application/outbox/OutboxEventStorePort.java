package com.pswied.loan.strasbourg.application.outbox;

import com.pswied.loan.strasbourg.domain.outbox.OutboxEvent;

import java.util.List;
import java.util.Optional;

public interface OutboxEventStorePort {
    void saveEvent(OutboxEvent event);

    void updateEvent(OutboxEvent event);

    List<OutboxEvent> findPendingEvents();

    Optional<OutboxEvent> findByEventId(String eventId);
}
