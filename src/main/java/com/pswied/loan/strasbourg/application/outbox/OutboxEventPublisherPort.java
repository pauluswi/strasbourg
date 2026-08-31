package com.pswied.loan.strasbourg.application.outbox;

import com.pswied.loan.strasbourg.domain.outbox.OutboxEvent;

public interface OutboxEventPublisherPort {
    void publish(OutboxEvent event);
}
