package com.pswied.loan.strasbourg.application.loanorigination;

import com.pswied.loan.strasbourg.application.audit.LoanApplicationAuditTrailStorePort;
import com.pswied.loan.strasbourg.application.merchantidentity.MerchantIdentityValidationService;
import com.pswied.loan.strasbourg.application.outbox.OutboxEventStorePort;
import com.pswied.loan.strasbourg.domain.audit.LoanApplicationAuditTrailEntry;
import com.pswied.loan.strasbourg.domain.loanorigination.CreditAssessmentStatus;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanApplication;
import com.pswied.loan.strasbourg.domain.loanorigination.EligibilityAssessmentStatus;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanApplicationJourneyResult;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanApplicationJourneyStage;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanApplicationLifecycleStage;
import com.pswied.loan.strasbourg.domain.loanorigination.ApplicantVerificationStatus;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanApplicationStatus;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanOriginationDecision;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanOriginationDecisionReasonCode;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanApplicationSubmissionRequest;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanApplicationSubmissionResult;
import com.pswied.loan.strasbourg.domain.merchantidentity.MerchantIdentityStatus;
import com.pswied.loan.strasbourg.domain.merchantidentity.MerchantIdentityValidationRequest;
import com.pswied.loan.strasbourg.domain.outbox.OutboxEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class LoanApplicationService {

    private final EligibilityAssessmentPort eligibilityAssessmentPort;
    private final CreditAssessmentPort creditAssessmentPort;
    private final ApplicantVerificationPort applicantVerificationPort;
    private final MerchantIdentityValidationService merchantIdentityValidationService;
    private final LoanApplicationStorePort loanApplicationStorePort;
    private final LoanApplicationAuditTrailStorePort loanApplicationAuditTrailStorePort;
    private final OutboxEventStorePort outboxEventStorePort;

    @Inject
    public LoanApplicationService(
            EligibilityAssessmentPort eligibilityAssessmentPort,
            CreditAssessmentPort creditAssessmentPort,
            ApplicantVerificationPort applicantVerificationPort,
            MerchantIdentityValidationService merchantIdentityValidationService,
            LoanApplicationStorePort loanApplicationStorePort,
            LoanApplicationAuditTrailStorePort loanApplicationAuditTrailStorePort,
            OutboxEventStorePort outboxEventStorePort
    ) {
        this.eligibilityAssessmentPort = eligibilityAssessmentPort;
        this.creditAssessmentPort = creditAssessmentPort;
        this.applicantVerificationPort = applicantVerificationPort;
        this.merchantIdentityValidationService = merchantIdentityValidationService;
        this.loanApplicationStorePort = loanApplicationStorePort;
        this.loanApplicationAuditTrailStorePort = loanApplicationAuditTrailStorePort;
        this.outboxEventStorePort = outboxEventStorePort;
    }

    public LoanApplicationSubmissionResult submit(LoanApplicationSubmissionRequest request) {
        String loanApplicationId = UUID.randomUUID().toString();
        Instant submittedAt = Instant.now();
        var eligibilityAssessment = eligibilityAssessmentPort.assess(
                request.amount(),
                request.tenorMonths()
        );
        var creditAssessment = creditAssessmentPort.assess(
                request.applicantName().trim(),
                request.amount(),
                request.tenorMonths()
        );
        var applicantVerification = applicantVerificationPort.verify(
                request.applicantName().trim(),
                request.amount(),
                request.tenorMonths()
        );
        var merchantVerification = merchantIdentityValidationService.validate(
                new MerchantIdentityValidationRequest(
                        request.merchantId().trim(),
                        request.merchantLegalName().trim(),
                        request.merchantTaxNumber().trim()
                )
        );
        Instant verifiedAt = merchantVerification.validatedAt();
        var decision = decide(
                eligibilityAssessment.status(),
                creditAssessment.status(),
                applicantVerification.status(),
                merchantVerification.status()
        );
        Instant decidedAt = Instant.now();

        LoanApplication loanApplication = new LoanApplication(
                loanApplicationId,
                request.applicantName().trim(),
                request.merchantId().trim(),
                request.amount(),
                request.tenorMonths(),
                LoanApplicationStatus.DECIDED,
                LoanApplicationLifecycleStage.DECIDED,
                decision.decision(),
                decision.reasonCode(),
                eligibilityAssessment.status(),
                eligibilityAssessment.reason(),
                creditAssessment.status(),
                creditAssessment.reason(),
                applicantVerification.status(),
                applicantVerification.reason(),
                merchantVerification.status(),
                merchantVerification.reason(),
                merchantVerification.sourceSystem(),
                merchantVerification.externalReference(),
                submittedAt,
                verifiedAt,
                decidedAt
        );
        loanApplicationStorePort.save(loanApplication);
        saveLifecycleAuditAndOutbox(loanApplication);

        return new LoanApplicationSubmissionResult(
                loanApplication.loanApplicationId(),
                loanApplication.applicantName(),
                loanApplication.merchantId(),
                loanApplication.amount(),
                loanApplication.tenorMonths(),
                loanApplication.status(),
                loanApplication.decision(),
                loanApplication.decisionReasonCode(),
                loanApplication.eligibilityStatus(),
                loanApplication.eligibilityReason(),
                loanApplication.creditAssessmentStatus(),
                loanApplication.creditAssessmentReason(),
                loanApplication.applicantVerificationStatus(),
                loanApplication.applicantVerificationReason(),
                loanApplication.merchantVerificationStatus(),
                loanApplication.merchantVerificationReason(),
                loanApplication.merchantVerificationSourceSystem(),
                loanApplication.merchantVerificationReference(),
                loanApplication.verifiedAt(),
                loanApplication.submittedAt()
        );
    }

    public Optional<LoanApplicationJourneyResult> getById(String loanApplicationId) {
        return loanApplicationStorePort.findByLoanApplicationId(loanApplicationId)
                .map(loanApplication -> {
                    List<LoanApplicationJourneyStage> journey = loanApplicationAuditTrailStorePort
                            .findByLoanApplicationId(loanApplicationId)
                            .stream()
                            .map(entry -> new LoanApplicationJourneyStage(
                                    entry.lifecycleStage(),
                                    entry.eventType(),
                                    entry.payload(),
                                    entry.createdAt()
                            ))
                            .toList();

                    return new LoanApplicationJourneyResult(
                            loanApplication.loanApplicationId(),
                            loanApplication.applicantName(),
                            loanApplication.merchantId(),
                            loanApplication.amount(),
                            loanApplication.tenorMonths(),
                            loanApplication.status(),
                            loanApplication.lifecycleStage(),
                            loanApplication.decision(),
                            loanApplication.decisionReasonCode(),
                            loanApplication.eligibilityStatus(),
                            loanApplication.eligibilityReason(),
                            loanApplication.creditAssessmentStatus(),
                            loanApplication.creditAssessmentReason(),
                            loanApplication.applicantVerificationStatus(),
                            loanApplication.applicantVerificationReason(),
                            loanApplication.merchantVerificationStatus(),
                            loanApplication.merchantVerificationReason(),
                            loanApplication.merchantVerificationSourceSystem(),
                            loanApplication.merchantVerificationReference(),
                            loanApplication.submittedAt(),
                            loanApplication.verifiedAt(),
                            loanApplication.decidedAt(),
                            journey
                    );
                });
    }

    private void saveLifecycleAuditAndOutbox(LoanApplication loanApplication) {
        saveStage(loanApplication, LoanApplicationLifecycleStage.SUBMITTED, "LoanApplicationSubmitted", loanApplication.submittedAt());
        saveStage(loanApplication, LoanApplicationLifecycleStage.VERIFIED, "LoanApplicationVerified", loanApplication.verifiedAt());
        saveStage(loanApplication, LoanApplicationLifecycleStage.DECIDED, "LoanApplicationDecided", loanApplication.decidedAt());
    }

    private void saveStage(
            LoanApplication loanApplication,
            LoanApplicationLifecycleStage stage,
            String eventType,
            Instant stageAt
    ) {
        loanApplicationAuditTrailStorePort.save(new LoanApplicationAuditTrailEntry(
                UUID.randomUUID().toString(),
                loanApplication.loanApplicationId(),
                stage.name(),
                eventType,
                lifecyclePayload(loanApplication, stage, stageAt),
                stageAt
        ));
        outboxEventStorePort.saveEvent(OutboxEvent.pending(
                UUID.randomUUID().toString(),
                eventType,
                loanApplication.loanApplicationId(),
                lifecyclePayload(loanApplication, stage, stageAt),
                stageAt
        ));
    }

    private String lifecyclePayload(
            LoanApplication loanApplication,
            LoanApplicationLifecycleStage stage,
            Instant stageAt
    ) {
        return """
                {"loanApplicationId":"%s","lifecycleStage":"%s","status":"%s","decision":"%s","decisionReasonCode":"%s","eligibilityStatus":"%s","creditAssessmentStatus":"%s","applicantVerificationStatus":"%s","merchantVerificationStatus":"%s","merchantId":"%s","amount":"%s","tenorMonths":%d,"at":"%s"}
                """.formatted(
                escape(loanApplication.loanApplicationId()),
                stage.name(),
                loanApplication.status().name(),
                loanApplication.decision().name(),
                loanApplication.decisionReasonCode().name(),
                loanApplication.eligibilityStatus().name(),
                loanApplication.creditAssessmentStatus().name(),
                loanApplication.applicantVerificationStatus().name(),
                loanApplication.merchantVerificationStatus().name(),
                escape(loanApplication.merchantId()),
                loanApplication.amount().toPlainString(),
                loanApplication.tenorMonths(),
                stageAt.toString()
        );
    }

    private DecisionResult decide(
            EligibilityAssessmentStatus eligibilityStatus,
            CreditAssessmentStatus creditStatus,
            ApplicantVerificationStatus applicantStatus,
            MerchantIdentityStatus merchantStatus
    ) {
        if (eligibilityStatus == EligibilityAssessmentStatus.INELIGIBLE) {
            return new DecisionResult(
                    LoanOriginationDecision.REJECTED,
                    LoanOriginationDecisionReasonCode.ELIGIBILITY_INELIGIBLE
            );
        }
        if (creditStatus == CreditAssessmentStatus.REJECTED) {
            return new DecisionResult(
                    LoanOriginationDecision.REJECTED,
                    LoanOriginationDecisionReasonCode.CREDIT_REJECTED
            );
        }
        if (applicantStatus == ApplicantVerificationStatus.REJECTED) {
            return new DecisionResult(
                    LoanOriginationDecision.REJECTED,
                    LoanOriginationDecisionReasonCode.APPLICANT_REJECTED
            );
        }
        if (merchantStatus == MerchantIdentityStatus.REJECTED) {
            return new DecisionResult(
                    LoanOriginationDecision.REJECTED,
                    LoanOriginationDecisionReasonCode.MERCHANT_REJECTED
            );
        }
        if (creditStatus == CreditAssessmentStatus.MANUAL_REVIEW) {
            return new DecisionResult(
                    LoanOriginationDecision.MANUAL_REVIEW,
                    LoanOriginationDecisionReasonCode.CREDIT_MANUAL_REVIEW_REQUIRED
            );
        }
        if (applicantStatus == ApplicantVerificationStatus.MANUAL_REVIEW) {
            return new DecisionResult(
                    LoanOriginationDecision.MANUAL_REVIEW,
                    LoanOriginationDecisionReasonCode.APPLICANT_MANUAL_REVIEW_REQUIRED
            );
        }
        if (merchantStatus == MerchantIdentityStatus.MANUAL_REVIEW) {
            return new DecisionResult(
                    LoanOriginationDecision.MANUAL_REVIEW,
                    LoanOriginationDecisionReasonCode.MERCHANT_MANUAL_REVIEW_REQUIRED
            );
        }
        if (eligibilityStatus == EligibilityAssessmentStatus.MANUAL_REVIEW) {
            return new DecisionResult(
                    LoanOriginationDecision.MANUAL_REVIEW,
                    LoanOriginationDecisionReasonCode.ELIGIBILITY_MANUAL_REVIEW_REQUIRED
            );
        }
        return new DecisionResult(
                LoanOriginationDecision.APPROVED,
                LoanOriginationDecisionReasonCode.ALL_CHECKS_PASSED
        );
    }

    private record DecisionResult(
            LoanOriginationDecision decision,
            LoanOriginationDecisionReasonCode reasonCode
    ) {
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
