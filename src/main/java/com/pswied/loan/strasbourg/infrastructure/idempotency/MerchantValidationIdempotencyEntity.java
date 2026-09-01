package com.pswied.loan.strasbourg.infrastructure.idempotency;

import com.pswied.loan.strasbourg.domain.idempotency.MerchantValidationIdempotencyEntry;
import com.pswied.loan.strasbourg.domain.merchantidentity.MerchantIdentityStatus;
import com.pswied.loan.strasbourg.domain.merchantidentity.MerchantIdentityValidationResult;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "merchant_validation_idempotency")
public class MerchantValidationIdempotencyEntity extends PanacheEntityBase {

    @Id
    @Column(name = "idempotency_key", nullable = false, updatable = false, length = 128)
    public String idempotencyKey;

    @Column(name = "request_fingerprint", nullable = false, length = 512)
    public String requestFingerprint;

    @Column(name = "merchant_id", nullable = false, length = 128)
    public String merchantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    public MerchantIdentityStatus status;

    @Column(name = "reason", columnDefinition = "TEXT")
    public String reason;

    @Column(name = "source_system", nullable = false, length = 64)
    public String sourceSystem;

    @Column(name = "external_reference", nullable = false, length = 128)
    public String externalReference;

    @Column(name = "validated_at", nullable = false)
    public Instant validatedAt;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    public static MerchantValidationIdempotencyEntity fromDomain(
            String idempotencyKey,
            String requestFingerprint,
            MerchantIdentityValidationResult result
    ) {
        MerchantValidationIdempotencyEntity entity = new MerchantValidationIdempotencyEntity();
        entity.idempotencyKey = idempotencyKey;
        entity.requestFingerprint = requestFingerprint;
        entity.merchantId = result.merchantId();
        entity.status = result.status();
        entity.reason = result.reason();
        entity.sourceSystem = result.sourceSystem();
        entity.externalReference = result.externalReference();
        entity.validatedAt = result.validatedAt();
        entity.createdAt = Instant.now();
        return entity;
    }

    public MerchantValidationIdempotencyEntry toDomain() {
        return new MerchantValidationIdempotencyEntry(
                idempotencyKey,
                requestFingerprint,
                new MerchantIdentityValidationResult(
                        merchantId,
                        status,
                        reason,
                        sourceSystem,
                        externalReference,
                        validatedAt
                )
        );
    }
}
