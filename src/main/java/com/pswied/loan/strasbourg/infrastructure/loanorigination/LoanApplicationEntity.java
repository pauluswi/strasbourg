package com.pswied.loan.strasbourg.infrastructure.loanorigination;

import com.pswied.loan.strasbourg.domain.loanorigination.ApplicantVerificationStatus;
import com.pswied.loan.strasbourg.domain.loanorigination.CreditAssessmentStatus;
import com.pswied.loan.strasbourg.domain.loanorigination.EligibilityAssessmentStatus;
import com.pswied.loan.strasbourg.domain.loanorigination.FraudAssessmentStatus;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanApplication;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanApplicationLifecycleStage;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanApplicationStatus;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanOriginationDecision;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanOriginationDecisionReasonCode;
import com.pswied.loan.strasbourg.domain.merchantidentity.MerchantIdentityStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "loan_applications")
public class LoanApplicationEntity extends PanacheEntityBase {

    @Id
    @Column(name = "loan_application_id", nullable = false, updatable = false, length = 64)
    public String loanApplicationId;

    @Column(name = "applicant_name", nullable = false, length = 256)
    public String applicantName;

    @Column(name = "merchant_id", nullable = false, length = 128)
    public String merchantId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    public BigDecimal amount;

    @Column(name = "tenor_months", nullable = false)
    public int tenorMonths;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    public LoanApplicationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "lifecycle_stage", nullable = false, length = 32)
    public LoanApplicationLifecycleStage lifecycleStage;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false, length = 32)
    public LoanOriginationDecision decision;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision_reason_code", nullable = false, length = 64)
    public LoanOriginationDecisionReasonCode decisionReasonCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "eligibility_status", nullable = false, length = 32)
    public EligibilityAssessmentStatus eligibilityStatus;

    @Column(name = "eligibility_reason", columnDefinition = "TEXT")
    public String eligibilityReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "credit_assessment_status", nullable = false, length = 32)
    public CreditAssessmentStatus creditAssessmentStatus;

    @Column(name = "credit_assessment_reason", columnDefinition = "TEXT")
    public String creditAssessmentReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "fraud_assessment_status", nullable = false, length = 32)
    public FraudAssessmentStatus fraudAssessmentStatus;

    @Column(name = "fraud_assessment_reason", columnDefinition = "TEXT")
    public String fraudAssessmentReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "applicant_verification_status", nullable = false, length = 32)
    public ApplicantVerificationStatus applicantVerificationStatus;

    @Column(name = "applicant_verification_reason", columnDefinition = "TEXT")
    public String applicantVerificationReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "merchant_verification_status", nullable = false, length = 32)
    public MerchantIdentityStatus merchantVerificationStatus;

    @Column(name = "merchant_verification_reason", columnDefinition = "TEXT")
    public String merchantVerificationReason;

    @Column(name = "merchant_verification_source_system", nullable = false, length = 64)
    public String merchantVerificationSourceSystem;

    @Column(name = "merchant_verification_reference", nullable = false, length = 128)
    public String merchantVerificationReference;

    @Column(name = "submitted_at", nullable = false)
    public Instant submittedAt;

    @Column(name = "verified_at", nullable = false)
    public Instant verifiedAt;

    @Column(name = "decided_at", nullable = false)
    public Instant decidedAt;

    public static LoanApplicationEntity fromDomain(LoanApplication loanApplication) {
        LoanApplicationEntity entity = new LoanApplicationEntity();
        entity.loanApplicationId = loanApplication.loanApplicationId();
        entity.applicantName = loanApplication.applicantName();
        entity.merchantId = loanApplication.merchantId();
        entity.amount = loanApplication.amount();
        entity.tenorMonths = loanApplication.tenorMonths();
        entity.status = loanApplication.status();
        entity.lifecycleStage = loanApplication.lifecycleStage();
        entity.decision = loanApplication.decision();
        entity.decisionReasonCode = loanApplication.decisionReasonCode();
        entity.eligibilityStatus = loanApplication.eligibilityStatus();
        entity.eligibilityReason = loanApplication.eligibilityReason();
        entity.creditAssessmentStatus = loanApplication.creditAssessmentStatus();
        entity.creditAssessmentReason = loanApplication.creditAssessmentReason();
        entity.fraudAssessmentStatus = loanApplication.fraudAssessmentStatus();
        entity.fraudAssessmentReason = loanApplication.fraudAssessmentReason();
        entity.applicantVerificationStatus = loanApplication.applicantVerificationStatus();
        entity.applicantVerificationReason = loanApplication.applicantVerificationReason();
        entity.merchantVerificationStatus = loanApplication.merchantVerificationStatus();
        entity.merchantVerificationReason = loanApplication.merchantVerificationReason();
        entity.merchantVerificationSourceSystem = loanApplication.merchantVerificationSourceSystem();
        entity.merchantVerificationReference = loanApplication.merchantVerificationReference();
        entity.submittedAt = loanApplication.submittedAt();
        entity.verifiedAt = loanApplication.verifiedAt();
        entity.decidedAt = loanApplication.decidedAt();
        return entity;
    }

    public LoanApplication toDomain() {
        return new LoanApplication(
                loanApplicationId,
                applicantName,
                merchantId,
                amount,
                tenorMonths,
                status,
                lifecycleStage,
                decision,
                decisionReasonCode,
                eligibilityStatus,
                eligibilityReason,
                creditAssessmentStatus,
                creditAssessmentReason,
                fraudAssessmentStatus,
                fraudAssessmentReason,
                applicantVerificationStatus,
                applicantVerificationReason,
                merchantVerificationStatus,
                merchantVerificationReason,
                merchantVerificationSourceSystem,
                merchantVerificationReference,
                submittedAt,
                verifiedAt,
                decidedAt
        );
    }
}
