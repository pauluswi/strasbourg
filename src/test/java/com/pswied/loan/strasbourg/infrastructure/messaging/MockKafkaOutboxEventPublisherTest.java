package com.pswied.loan.strasbourg.infrastructure.messaging;

import com.pswied.loan.strasbourg.domain.outbox.OutboxEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class MockKafkaOutboxEventPublisherTest {

    @Test
    void publishesTheSameEventOnlyOnce() {
        MockKafkaOutboxEventPublisher publisher = new MockKafkaOutboxEventPublisher();
        OutboxEvent event = OutboxEvent.pending(
                "evt-2001",
                "MerchantIdentityValidated",
                "m-2001",
                "{\"status\":\"VERIFIED\"}",
                Instant.parse("2026-08-31T06:10:00Z")
        );

        publisher.publish(event);
        publisher.publish(event);

        assertThat(publisher.publishedCount()).isEqualTo(1);
        assertThat(publisher.hasPublished("evt-2001")).isTrue();
    }
}
