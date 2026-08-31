package com.pswied.loan.strasbourg.domain.outbox;

import java.time.Instant;

public final class OutboxEvent {

    private final String eventId;
    private final String eventType;
    private final String aggregateId;
    private final String payload;
    private final OutboxEventStatus status;
    private final int retryCount;
    private final Instant createdAt;
    private final Instant publishedAt;
    private final String lastError;

    private OutboxEvent(
            String eventId,
            String eventType,
            String aggregateId,
            String payload,
            OutboxEventStatus status,
            int retryCount,
            Instant createdAt,
            Instant publishedAt,
            String lastError
    ) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.payload = payload;
        this.status = status;
        this.retryCount = retryCount;
        this.createdAt = createdAt;
        this.publishedAt = publishedAt;
        this.lastError = lastError;
    }

    public static OutboxEvent pending(
            String eventId,
            String eventType,
            String aggregateId,
            String payload,
            Instant createdAt
    ) {
        return new OutboxEvent(
                eventId,
                eventType,
                aggregateId,
                payload,
                OutboxEventStatus.PENDING,
                0,
                createdAt,
                null,
                null
        );
    }

    public OutboxEvent markPublished(Instant publishedAt) {
        return new OutboxEvent(
                eventId,
                eventType,
                aggregateId,
                payload,
                OutboxEventStatus.PUBLISHED,
                retryCount,
                createdAt,
                publishedAt,
                null
        );
    }

    public OutboxEvent markRetry(String errorMessage) {
        return new OutboxEvent(
                eventId,
                eventType,
                aggregateId,
                payload,
                OutboxEventStatus.PENDING,
                retryCount + 1,
                createdAt,
                null,
                errorMessage
        );
    }

    public String eventId() {
        return eventId;
    }

    public String eventType() {
        return eventType;
    }

    public String aggregateId() {
        return aggregateId;
    }

    public String payload() {
        return payload;
    }

    public OutboxEventStatus status() {
        return status;
    }

    public int retryCount() {
        return retryCount;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant publishedAt() {
        return publishedAt;
    }

    public String lastError() {
        return lastError;
    }
}
