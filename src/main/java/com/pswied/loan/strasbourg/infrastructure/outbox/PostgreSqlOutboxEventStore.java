package com.pswied.loan.strasbourg.infrastructure.outbox;

import com.pswied.loan.strasbourg.application.outbox.OutboxEventStorePort;
import com.pswied.loan.strasbourg.domain.outbox.OutboxEvent;
import com.pswied.loan.strasbourg.domain.outbox.OutboxEventStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PostgreSqlOutboxEventStore implements OutboxEventStorePort, PanacheRepositoryBase<OutboxEventEntity, String> {

    @Override
    @Transactional
    public void saveEvent(OutboxEvent event) {
        persist(OutboxEventEntity.fromDomain(event));
    }

    @Override
    @Transactional
    public void updateEvent(OutboxEvent event) {
        OutboxEventEntity managed = findById(event.eventId());
        if (managed == null) {
            persist(OutboxEventEntity.fromDomain(event));
            return;
        }

        managed.eventType = event.eventType();
        managed.aggregateId = event.aggregateId();
        managed.payload = event.payload();
        managed.status = event.status();
        managed.retryCount = event.retryCount();
        managed.createdAt = event.createdAt();
        managed.publishedAt = event.publishedAt();
        managed.lastRetriedAt = event.lastRetriedAt();
        managed.lastError = event.lastError();
    }

    @Override
    public List<OutboxEvent> findPendingEvents() {
        return find("status = ?1 order by createdAt", OutboxEventStatus.PENDING)
                .stream()
                .map(OutboxEventEntity.class::cast)
                .map(OutboxEventEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<OutboxEvent> findByEventId(String eventId) {
        return findByIdOptional(eventId).map(OutboxEventEntity::toDomain);
    }
}
