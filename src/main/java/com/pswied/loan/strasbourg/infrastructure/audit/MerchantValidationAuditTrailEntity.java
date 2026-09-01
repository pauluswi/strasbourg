package com.pswied.loan.strasbourg.infrastructure.audit;

import com.pswied.loan.strasbourg.domain.audit.MerchantValidationAuditTrailEntry;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "merchant_validation_audit_trail")
public class MerchantValidationAuditTrailEntity extends PanacheEntityBase {

    @Id
    @Column(name = "audit_id", nullable = false, updatable = false, length = 64)
    public String auditId;

    @Column(name = "idempotency_key", nullable = false, length = 128)
    public String idempotencyKey;

    @Column(name = "merchant_id", nullable = false, length = 128)
    public String merchantId;

    @Column(name = "request_payload", nullable = false, columnDefinition = "TEXT")
    public String requestPayload;

    @Column(name = "mapped_sap_response", nullable = false, columnDefinition = "TEXT")
    public String mappedSapResponse;

    @Column(name = "decision_status", nullable = false, length = 32)
    public String decisionStatus;

    @Column(name = "decision_reason", columnDefinition = "TEXT")
    public String decisionReason;

    @Column(name = "replayed", nullable = false)
    public boolean replayed;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    public static MerchantValidationAuditTrailEntity fromDomain(MerchantValidationAuditTrailEntry entry) {
        MerchantValidationAuditTrailEntity entity = new MerchantValidationAuditTrailEntity();
        entity.auditId = entry.auditId();
        entity.idempotencyKey = entry.idempotencyKey();
        entity.merchantId = entry.merchantId();
        entity.requestPayload = entry.requestPayload();
        entity.mappedSapResponse = entry.mappedSapResponse();
        entity.decisionStatus = entry.decisionStatus();
        entity.decisionReason = entry.decisionReason();
        entity.replayed = entry.replayed();
        entity.createdAt = entry.createdAt();
        return entity;
    }

    public MerchantValidationAuditTrailEntry toDomain() {
        return new MerchantValidationAuditTrailEntry(
                auditId,
                idempotencyKey,
                merchantId,
                requestPayload,
                mappedSapResponse,
                decisionStatus,
                decisionReason,
                replayed,
                createdAt
        );
    }
}
