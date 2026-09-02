package com.pswied.loan.strasbourg.application.loanorigination;

import com.pswied.loan.strasbourg.application.merchantidentity.MerchantIdentityValidationPort;
import com.pswied.loan.strasbourg.application.merchantidentity.MerchantIdentityValidationService;
import com.pswied.loan.strasbourg.domain.loanorigination.ApplicantVerificationResult;
import com.pswied.loan.strasbourg.domain.loanorigination.ApplicantVerificationStatus;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanApplicationStatus;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanOriginationDecision;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanOriginationDecisionReasonCode;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanApplicationSubmissionRequest;
import com.pswied.loan.strasbourg.domain.merchantidentity.MerchantIdentityStatus;
import com.pswied.loan.strasbourg.domain.merchantidentity.MerchantIdentityValidationResult;
import com.pswied.loan.strasbourg.infrastructure.outbox.InMemoryOutboxEventStore;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class LoanApplicationServiceTest {

    @Test
    void approvesWhenApplicantAndMerchantChecksPass() {
        ApplicantVerificationPort applicantVerificationPort = (applicantName, amount, tenorMonths) ->
                new ApplicantVerificationResult(ApplicantVerificationStatus.PASSED, "Applicant passed mock verification");
        MerchantIdentityValidationPort merchantValidationPort = verificationPortWithStatus(MerchantIdentityStatus.VERIFIED);
        MerchantIdentityValidationService merchantService = merchantService(merchantValidationPort);
        LoanApplicationService service = new LoanApplicationService(applicantVerificationPort, merchantService);

        var result = service.submit(sampleRequest());

        assertThat(result.loanApplicationId()).isNotBlank();
        assertThat(result.applicantName()).isEqualTo("Alice Applicant");
        assertThat(result.merchantId()).isEqualTo("m-4001");
        assertThat(result.amount()).isEqualByComparingTo("15000.00");
        assertThat(result.tenorMonths()).isEqualTo(24);
        assertThat(result.status()).isEqualTo(LoanApplicationStatus.SUBMITTED);
        assertThat(result.decision()).isEqualTo(LoanOriginationDecision.APPROVED);
        assertThat(result.decisionReasonCode()).isEqualTo(LoanOriginationDecisionReasonCode.ALL_CHECKS_PASSED);
        assertThat(result.applicantVerificationStatus()).isEqualTo(ApplicantVerificationStatus.PASSED);
        assertThat(result.applicantVerificationReason()).isEqualTo("Applicant passed mock verification");
        assertThat(result.merchantVerificationStatus()).isEqualTo(MerchantIdentityStatus.VERIFIED);
        assertThat(result.merchantVerificationReason()).contains("confirmed");
        assertThat(result.merchantVerificationSourceSystem()).isEqualTo("SAP_S4");
        assertThat(result.merchantVerificationReference()).startsWith("SAP-S4-VER-");
        assertThat(result.verifiedAt()).isEqualTo(Instant.parse("2026-09-03T00:00:00Z"));
        assertThat(result.submittedAt()).isNotNull();
    }

    @Test
    void rejectsWhenApplicantCheckFails() {
        ApplicantVerificationPort applicantVerificationPort = (applicantName, amount, tenorMonths) ->
                new ApplicantVerificationResult(ApplicantVerificationStatus.REJECTED, "Applicant was flagged");
        MerchantIdentityValidationService merchantService = merchantService(verificationPortWithStatus(MerchantIdentityStatus.VERIFIED));
        LoanApplicationService service = new LoanApplicationService(applicantVerificationPort, merchantService);

        var result = service.submit(sampleRequest());

        assertThat(result.decision()).isEqualTo(LoanOriginationDecision.REJECTED);
        assertThat(result.decisionReasonCode()).isEqualTo(LoanOriginationDecisionReasonCode.APPLICANT_REJECTED);
    }

    @Test
    void routesToManualReviewWhenMerchantNeedsManualReview() {
        ApplicantVerificationPort applicantVerificationPort = (applicantName, amount, tenorMonths) ->
                new ApplicantVerificationResult(ApplicantVerificationStatus.PASSED, "Applicant passed mock verification");
        MerchantIdentityValidationService merchantService = merchantService(verificationPortWithStatus(MerchantIdentityStatus.MANUAL_REVIEW));
        LoanApplicationService service = new LoanApplicationService(applicantVerificationPort, merchantService);

        var result = service.submit(sampleRequest());

        assertThat(result.decision()).isEqualTo(LoanOriginationDecision.MANUAL_REVIEW);
        assertThat(result.decisionReasonCode()).isEqualTo(LoanOriginationDecisionReasonCode.MERCHANT_MANUAL_REVIEW_REQUIRED);
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

    private MerchantIdentityValidationService merchantService(MerchantIdentityValidationPort port) {
        return new MerchantIdentityValidationService(port, new InMemoryOutboxEventStore());
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
}
