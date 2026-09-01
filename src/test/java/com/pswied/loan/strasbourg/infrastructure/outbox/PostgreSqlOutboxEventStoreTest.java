package com.pswied.loan.strasbourg.infrastructure.outbox;

import com.pswied.loan.strasbourg.application.outbox.OutboxEventStorePort;
import com.pswied.loan.strasbourg.domain.outbox.OutboxEvent;
import com.pswied.loan.strasbourg.domain.outbox.OutboxEventStatus;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class PostgreSqlOutboxEventStoreTest {

    @Inject
    OutboxEventStorePort store;

    @Test
    void persistsAndReadsOutboxEvent() {
        OutboxEvent event = OutboxEvent.pending(
                "evt-store-1001",
                "MerchantIdentityValidated",
                "m-store-1001",
                "{\"status\":\"VERIFIED\"}",
                Instant.parse("2026-09-01T12:00:00Z")
        );

        store.saveEvent(event);

        var persisted = store.findByEventId("evt-store-1001");
        assertThat(persisted).isPresent();
        assertThat(persisted.orElseThrow().status()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(store.findPendingEvents())
                .extracting(OutboxEvent::eventId)
                .contains("evt-store-1001");
    }
}
