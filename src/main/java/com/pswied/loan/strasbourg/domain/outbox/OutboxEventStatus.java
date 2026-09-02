package com.pswied.loan.strasbourg.domain.outbox;

public enum OutboxEventStatus {
    PENDING,
    PUBLISHED,
    DEAD_LETTER
}
