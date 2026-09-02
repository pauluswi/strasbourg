package com.pswied.loan.strasbourg.infrastructure.audit;

import com.pswied.loan.strasbourg.domain.audit.LoanApplicationAuditTrailEntry;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "loan_application_audit_trail")
public class LoanApplicationAuditTrailEntity extends PanacheEntityBase {

    @Id
    @Column(name = "audit_id", nullable = false, updatable = false, length = 64)
    public String auditId;

    @Column(name = "loan_application_id", nullable = false, length = 64)
    public String loanApplicationId;

    @Column(name = "lifecycle_stage", nullable = false, length = 32)
    public String lifecycleStage;

    @Column(name = "event_type", nullable = false, length = 128)
    public String eventType;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    public String payload;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    public static LoanApplicationAuditTrailEntity fromDomain(LoanApplicationAuditTrailEntry entry) {
        LoanApplicationAuditTrailEntity entity = new LoanApplicationAuditTrailEntity();
        entity.auditId = entry.auditId();
        entity.loanApplicationId = entry.loanApplicationId();
        entity.lifecycleStage = entry.lifecycleStage();
        entity.eventType = entry.eventType();
        entity.payload = entry.payload();
        entity.createdAt = entry.createdAt();
        return entity;
    }

    public LoanApplicationAuditTrailEntry toDomain() {
        return new LoanApplicationAuditTrailEntry(
                auditId,
                loanApplicationId,
                lifecycleStage,
                eventType,
                payload,
                createdAt
        );
    }
}
