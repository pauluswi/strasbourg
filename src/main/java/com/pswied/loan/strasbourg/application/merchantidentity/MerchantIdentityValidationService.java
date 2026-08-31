package com.pswied.loan.strasbourg.application.merchantidentity;

import com.pswied.loan.strasbourg.application.outbox.OutboxEventStorePort;
import com.pswied.loan.strasbourg.domain.merchantidentity.MerchantIdentityValidationRequest;
import com.pswied.loan.strasbourg.domain.merchantidentity.MerchantIdentityValidationResult;
import com.pswied.loan.strasbourg.domain.outbox.OutboxEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class MerchantIdentityValidationService {

    private final MerchantIdentityValidationPort validationPort;
    private final OutboxEventStorePort outboxEventStorePort;

    @Inject
    public MerchantIdentityValidationService(
            MerchantIdentityValidationPort validationPort,
            OutboxEventStorePort outboxEventStorePort
    ) {
        this.validationPort = validationPort;
        this.outboxEventStorePort = outboxEventStorePort;
    }

    public MerchantIdentityValidationResult validate(MerchantIdentityValidationRequest request) {
        MerchantIdentityValidationResult result = validationPort.validate(request);
        outboxEventStorePort.saveEvent(createOutboxEvent(result));
        return result;
    }

    private OutboxEvent createOutboxEvent(MerchantIdentityValidationResult result) {
        return OutboxEvent.pending(
                UUID.randomUUID().toString(),
                "MerchantIdentityValidated",
                result.merchantId(),
                toPayload(result),
                Instant.now()
        );
    }

    private String toPayload(MerchantIdentityValidationResult result) {
        return """
                {"merchantId":"%s","status":"%s","sourceSystem":"%s","externalReference":"%s","reason":"%s","validatedAt":"%s"}
                """.formatted(
                escape(result.merchantId()),
                result.status().name(),
                escape(result.sourceSystem()),
                escape(result.externalReference()),
                escape(result.reason()),
                result.validatedAt().toString()
        );
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
