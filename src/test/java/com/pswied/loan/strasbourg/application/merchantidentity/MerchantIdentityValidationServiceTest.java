package com.pswied.loan.strasbourg.application.merchantidentity;

import com.pswied.loan.strasbourg.domain.merchantidentity.MerchantIdentityStatus;
import com.pswied.loan.strasbourg.domain.merchantidentity.MerchantIdentityValidationRequest;
import com.pswied.loan.strasbourg.domain.merchantidentity.MerchantIdentityValidationResult;
import com.pswied.loan.strasbourg.infrastructure.outbox.InMemoryOutboxEventStore;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class MerchantIdentityValidationServiceTest {

    @Test
    void createsOutboxEventAfterMerchantValidation() {
        MerchantIdentityValidationPort stubPort = request -> new MerchantIdentityValidationResult(
                request.merchantId(),
                MerchantIdentityStatus.VERIFIED,
                "validated",
                "SAP_S4",
                "SAP-S4-VER-" + request.merchantId(),
                Instant.parse("2026-08-31T07:00:00Z")
        );
        InMemoryOutboxEventStore outboxStore = new InMemoryOutboxEventStore();
        MerchantIdentityValidationService service = new MerchantIdentityValidationService(stubPort, outboxStore);

        service.validate(new MerchantIdentityValidationRequest("m-3001", "Acme", "TAX-3001"));

        assertThat(outboxStore.findPendingEvents()).hasSize(1);
        var event = outboxStore.findPendingEvents().getFirst();
        assertThat(event.eventType()).isEqualTo("MerchantIdentityValidated");
        assertThat(event.aggregateId()).isEqualTo("m-3001");
        assertThat(event.payload()).contains("\"status\":\"VERIFIED\"");
    }
}
