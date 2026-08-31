package com.pswied.loan.strasbourg.application.outbox;

import com.pswied.loan.strasbourg.domain.outbox.OutboxEvent;
import com.pswied.loan.strasbourg.domain.outbox.OutboxEventStatus;
import com.pswied.loan.strasbourg.infrastructure.messaging.MockKafkaOutboxEventPublisher;
import com.pswied.loan.strasbourg.infrastructure.outbox.InMemoryOutboxEventStore;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxPublisherServiceTest {

    @Test
    void publishesPendingEventAndMarksItPublished() {
        InMemoryOutboxEventStore store = new InMemoryOutboxEventStore();
        MockKafkaOutboxEventPublisher publisher = new MockKafkaOutboxEventPublisher();
        OutboxPublisherService service = new OutboxPublisherService(store, publisher);

        store.saveEvent(OutboxEvent.pending(
                "evt-1001",
                "MerchantIdentityValidated",
                "m-1001",
                "{\"status\":\"VERIFIED\"}",
                Instant.parse("2026-08-31T06:00:00Z")
        ));

        int published = service.publishPendingEvents();

        assertThat(published).isEqualTo(1);
        assertThat(store.findPendingEvents()).isEmpty();
        assertThat(store.findByEventId("evt-1001")).isPresent();
        assertThat(store.findByEventId("evt-1001").orElseThrow().status()).isEqualTo(OutboxEventStatus.PUBLISHED);
    }

    @Test
    void retriesEventOnMockKafkaFailure() {
        InMemoryOutboxEventStore store = new InMemoryOutboxEventStore();
        MockKafkaOutboxEventPublisher publisher = new MockKafkaOutboxEventPublisher();
        OutboxPublisherService service = new OutboxPublisherService(store, publisher);

        store.saveEvent(OutboxEvent.pending(
                "evt-1002",
                "MerchantIdentityValidated",
                "fail-publish-merchant",
                "{\"status\":\"VERIFIED\"}",
                Instant.parse("2026-08-31T06:05:00Z")
        ));

        int published = service.publishPendingEvents();

        assertThat(published).isZero();
        assertThat(store.findPendingEvents()).hasSize(1);
        var event = store.findByEventId("evt-1002").orElseThrow();
        assertThat(event.status()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(event.retryCount()).isEqualTo(1);
        assertThat(event.lastError()).contains("Mock Kafka publish failure");
    }
}
