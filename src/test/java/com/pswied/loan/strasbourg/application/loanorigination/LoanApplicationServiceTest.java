package com.pswied.loan.strasbourg.application.loanorigination;

import com.pswied.loan.strasbourg.application.audit.LoanApplicationAuditTrailStorePort;
import com.pswied.loan.strasbourg.application.merchantidentity.MerchantIdentityValidationPort;
import com.pswied.loan.strasbourg.application.merchantidentity.MerchantIdentityValidationService;
import com.pswied.loan.strasbourg.application.outbox.OutboxEventStorePort;
import com.pswied.loan.strasbourg.domain.audit.LoanApplicationAuditTrailEntry;
import com.pswied.loan.strasbourg.domain.loanorigination.ApplicantVerificationResult;
import com.pswied.loan.strasbourg.domain.loanorigination.ApplicantVerificationStatus;
import com.pswied.loan.strasbourg.domain.loanorigination.EligibilityAssessmentResult;
import com.pswied.loan.strasbourg.domain.loanorigination.EligibilityAssessmentStatus;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanApplication;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanApplicationJourneyResult;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanApplicationLifecycleStage;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanApplicationStatus;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanApplicationSubmissionRequest;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanOriginationDecision;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanOriginationDecisionReasonCode;
import com.pswied.loan.strasbourg.domain.merchantidentity.MerchantIdentityStatus;
import com.pswied.loan.strasbourg.domain.merchantidentity.MerchantIdentityValidationResult;
import com.pswied.loan.strasbourg.domain.outbox.OutboxEvent;
import com.pswied.loan.strasbourg.infrastructure.outbox.InMemoryOutboxEventStore;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

class LoanApplicationServiceTest {

    @Test
    void approvesWhenAllChecksPass() {
        InMemoryLoanApplicationStore loanApplicationStore = new InMemoryLoanApplicationStore();
        InMemoryLoanApplicationAuditTrailStore auditTrailStore = new InMemoryLoanApplicationAuditTrailStore();
        InMemoryOutboxEventStore outboxEventStore = new InMemoryOutboxEventStore();
        LoanApplicationService service = new LoanApplicationService(
                eligibleAssessmentPort(),
                passedApplicantVerificationPort(),
                merchantService(verificationPortWithStatus(MerchantIdentityStatus.VERIFIED), outboxEventStore),
                loanApplicationStore,
                auditTrailStore,
                outboxEventStore
        );

        var result = service.submit(sampleRequest());

        assertThat(result.loanApplicationId()).isNotBlank();
        assertThat(result.applicantName()).isEqualTo("Alice Applicant");
        assertThat(result.merchantId()).isEqualTo("m-4001");
        assertThat(result.amount()).isEqualByComparingTo("15000.00");
        assertThat(result.tenorMonths()).isEqualTo(24);
        assertThat(result.status()).isEqualTo(LoanApplicationStatus.DECIDED);
        assertThat(result.decision()).isEqualTo(LoanOriginationDecision.APPROVED);
        assertThat(result.decisionReasonCode()).isEqualTo(LoanOriginationDecisionReasonCode.ALL_CHECKS_PASSED);
        assertThat(result.eligibilityStatus()).isEqualTo(EligibilityAssessmentStatus.ELIGIBLE);
        assertThat(result.eligibilityReason()).isEqualTo("Application passed initial eligibility policy");
        assertThat(result.applicantVerificationStatus()).isEqualTo(ApplicantVerificationStatus.PASSED);
        assertThat(result.merchantVerificationStatus()).isEqualTo(MerchantIdentityStatus.VERIFIED);
        assertThat(result.verifiedAt()).isEqualTo(Instant.parse("2026-09-03T00:00:00Z"));
        assertThat(result.submittedAt()).isNotNull();

        LoanApplication persisted = loanApplicationStore.findByLoanApplicationId(result.loanApplicationId()).orElseThrow();
        assertThat(persisted.lifecycleStage()).isEqualTo(LoanApplicationLifecycleStage.DECIDED);
        assertThat(persisted.eligibilityStatus()).isEqualTo(EligibilityAssessmentStatus.ELIGIBLE);
        assertThat(auditTrailStore.findByLoanApplicationId(result.loanApplicationId()))
                .extracting(LoanApplicationAuditTrailEntry::eventType)
                .containsExactly("LoanApplicationSubmitted", "LoanApplicationVerified", "LoanApplicationDecided");
        assertThat(outboxEventStore.findPendingEvents())
                .extracting(OutboxEvent::eventType)
                .contains("MerchantIdentityValidated", "LoanApplicationSubmitted", "LoanApplicationVerified", "LoanApplicationDecided");
    }

    @Test
    void rejectsWhenEligibilityFails() {
        InMemoryOutboxEventStore outboxEventStore = new InMemoryOutboxEventStore();
        LoanApplicationService service = new LoanApplicationService(
                (amount, tenorMonths) -> new EligibilityAssessmentResult(
                        EligibilityAssessmentStatus.INELIGIBLE,
                        "Below minimum policy threshold"
                ),
                passedApplicantVerificationPort(),
                merchantService(verificationPortWithStatus(MerchantIdentityStatus.VERIFIED), outboxEventStore),
                new InMemoryLoanApplicationStore(),
                new InMemoryLoanApplicationAuditTrailStore(),
                outboxEventStore
        );

        var result = service.submit(sampleRequest());

        assertThat(result.decision()).isEqualTo(LoanOriginationDecision.REJECTED);
        assertThat(result.decisionReasonCode()).isEqualTo(LoanOriginationDecisionReasonCode.ELIGIBILITY_INELIGIBLE);
        assertThat(result.eligibilityStatus()).isEqualTo(EligibilityAssessmentStatus.INELIGIBLE);
    }

    @Test
    void routesToManualReviewWhenEligibilityNeedsReview() {
        InMemoryOutboxEventStore outboxEventStore = new InMemoryOutboxEventStore();
        LoanApplicationService service = new LoanApplicationService(
                (amount, tenorMonths) -> new EligibilityAssessmentResult(
                        EligibilityAssessmentStatus.MANUAL_REVIEW,
                        "Threshold exceeded"
                ),
                passedApplicantVerificationPort(),
                merchantService(verificationPortWithStatus(MerchantIdentityStatus.VERIFIED), outboxEventStore),
                new InMemoryLoanApplicationStore(),
                new InMemoryLoanApplicationAuditTrailStore(),
                outboxEventStore
        );

        var result = service.submit(sampleRequest());

        assertThat(result.decision()).isEqualTo(LoanOriginationDecision.MANUAL_REVIEW);
        assertThat(result.decisionReasonCode()).isEqualTo(LoanOriginationDecisionReasonCode.ELIGIBILITY_MANUAL_REVIEW_REQUIRED);
        assertThat(result.eligibilityStatus()).isEqualTo(EligibilityAssessmentStatus.MANUAL_REVIEW);
    }

    @Test
    void rejectsWhenApplicantCheckFails() {
        InMemoryOutboxEventStore outboxEventStore = new InMemoryOutboxEventStore();
        LoanApplicationService service = new LoanApplicationService(
                eligibleAssessmentPort(),
                (applicantName, amount, tenorMonths) -> new ApplicantVerificationResult(
                        ApplicantVerificationStatus.REJECTED,
                        "Applicant was flagged"
                ),
                merchantService(verificationPortWithStatus(MerchantIdentityStatus.VERIFIED), outboxEventStore),
                new InMemoryLoanApplicationStore(),
                new InMemoryLoanApplicationAuditTrailStore(),
                outboxEventStore
        );

        var result = service.submit(sampleRequest());

        assertThat(result.decision()).isEqualTo(LoanOriginationDecision.REJECTED);
        assertThat(result.decisionReasonCode()).isEqualTo(LoanOriginationDecisionReasonCode.APPLICANT_REJECTED);
    }

    @Test
    void routesToManualReviewWhenMerchantNeedsManualReview() {
        InMemoryOutboxEventStore outboxEventStore = new InMemoryOutboxEventStore();
        LoanApplicationService service = new LoanApplicationService(
                eligibleAssessmentPort(),
                passedApplicantVerificationPort(),
                merchantService(verificationPortWithStatus(MerchantIdentityStatus.MANUAL_REVIEW), outboxEventStore),
                new InMemoryLoanApplicationStore(),
                new InMemoryLoanApplicationAuditTrailStore(),
                outboxEventStore
        );

        var result = service.submit(sampleRequest());

        assertThat(result.decision()).isEqualTo(LoanOriginationDecision.MANUAL_REVIEW);
        assertThat(result.decisionReasonCode()).isEqualTo(LoanOriginationDecisionReasonCode.MERCHANT_MANUAL_REVIEW_REQUIRED);
    }

    @Test
    void readsLoanApplicationJourneyFromPersistedLifecycle() {
        InMemoryLoanApplicationStore loanApplicationStore = new InMemoryLoanApplicationStore();
        InMemoryLoanApplicationAuditTrailStore auditTrailStore = new InMemoryLoanApplicationAuditTrailStore();
        InMemoryOutboxEventStore outboxEventStore = new InMemoryOutboxEventStore();
        LoanApplicationService service = new LoanApplicationService(
                eligibleAssessmentPort(),
                passedApplicantVerificationPort(),
                merchantService(verificationPortWithStatus(MerchantIdentityStatus.VERIFIED), outboxEventStore),
                loanApplicationStore,
                auditTrailStore,
                outboxEventStore
        );

        var submitted = service.submit(sampleRequest());
        LoanApplicationJourneyResult journey = service.getById(submitted.loanApplicationId()).orElseThrow();

        assertThat(journey.loanApplicationId()).isEqualTo(submitted.loanApplicationId());
        assertThat(journey.eligibilityStatus()).isEqualTo(EligibilityAssessmentStatus.ELIGIBLE);
        assertThat(journey.merchantVerificationStatus()).isEqualTo(MerchantIdentityStatus.VERIFIED);
        assertThat(journey.journey())
                .extracting(stage -> stage.eventType())
                .containsExactly("LoanApplicationSubmitted", "LoanApplicationVerified", "LoanApplicationDecided");
    }

    private EligibilityAssessmentPort eligibleAssessmentPort() {
        return (amount, tenorMonths) -> new EligibilityAssessmentResult(
                EligibilityAssessmentStatus.ELIGIBLE,
                "Application passed initial eligibility policy"
        );
    }

    private ApplicantVerificationPort passedApplicantVerificationPort() {
        return (applicantName, amount, tenorMonths) -> new ApplicantVerificationResult(
                ApplicantVerificationStatus.PASSED,
                "Applicant passed mock verification"
        );
    }

    private MerchantIdentityValidationPort verificationPortWithStatus(MerchantIdentityStatus status) {
        return request -> new MerchantIdentityValidationResult(
                request.merchantId(),
                status,
                "SAP S/4 confirmed the merchant identity",
                "SAP_S4",
                "SAP-S4-VER-" + request.merchantId(),
                Instant.parse("2026-09-03T00:00:00Z")
        );
    }

    private MerchantIdentityValidationService merchantService(MerchantIdentityValidationPort port, OutboxEventStorePort outboxEventStorePort) {
        return new MerchantIdentityValidationService(port, outboxEventStorePort);
    }

    private LoanApplicationSubmissionRequest sampleRequest() {
        return new LoanApplicationSubmissionRequest(
                "Alice Applicant",
                "m-4001",
                "Acme Merchant",
                "TAX-4001",
                new BigDecimal("15000.00"),
                24
        );
    }

    private static final class InMemoryLoanApplicationStore implements LoanApplicationStorePort {
        private final ConcurrentHashMap<String, LoanApplication> values = new ConcurrentHashMap<>();

        @Override
        public void save(LoanApplication loanApplication) {
            values.put(loanApplication.loanApplicationId(), loanApplication);
        }

        @Override
        public Optional<LoanApplication> findByLoanApplicationId(String loanApplicationId) {
            return Optional.ofNullable(values.get(loanApplicationId));
        }
    }

    private static final class InMemoryLoanApplicationAuditTrailStore implements LoanApplicationAuditTrailStorePort {
        private final List<LoanApplicationAuditTrailEntry> values = new ArrayList<>();

        @Override
        public void save(LoanApplicationAuditTrailEntry entry) {
            values.add(entry);
        }

        @Override
        public List<LoanApplicationAuditTrailEntry> findByLoanApplicationId(String loanApplicationId) {
            return values.stream()
                    .filter(entry -> entry.loanApplicationId().equals(loanApplicationId))
                    .toList();
        }
    }
}
