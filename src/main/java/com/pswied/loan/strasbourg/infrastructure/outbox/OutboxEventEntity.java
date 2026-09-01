package com.pswied.loan.strasbourg.infrastructure.outbox;

import com.pswied.loan.strasbourg.domain.outbox.OutboxEvent;
import com.pswied.loan.strasbourg.domain.outbox.OutboxEventStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "outbox_events")
public class OutboxEventEntity extends PanacheEntityBase {

    @Id
    @Column(name = "event_id", nullable = false, updatable = false, length = 64)
    public String eventId;

    @Column(name = "event_type", nullable = false, length = 128)
    public String eventType;

    @Column(name = "aggregate_id", nullable = false, length = 128)
    public String aggregateId;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    public String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    public OutboxEventStatus status;

    @Column(name = "retry_count", nullable = false)
    public int retryCount;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "published_at")
    public Instant publishedAt;

    @Column(name = "last_retried_at")
    public Instant lastRetriedAt;

    @Column(name = "last_error", columnDefinition = "TEXT")
    public String lastError;

    public static OutboxEventEntity fromDomain(OutboxEvent event) {
        OutboxEventEntity entity = new OutboxEventEntity();
        entity.eventId = event.eventId();
        entity.eventType = event.eventType();
        entity.aggregateId = event.aggregateId();
        entity.payload = event.payload();
        entity.status = event.status();
        entity.retryCount = event.retryCount();
        entity.createdAt = event.createdAt();
        entity.publishedAt = event.publishedAt();
        entity.lastRetriedAt = event.lastRetriedAt();
        entity.lastError = event.lastError();
        return entity;
    }

    public OutboxEvent toDomain() {
        return OutboxEvent.rehydrate(
                eventId,
                eventType,
                aggregateId,
                payload,
                status,
                retryCount,
                createdAt,
                publishedAt,
                lastRetriedAt,
                lastError
        );
    }
}
