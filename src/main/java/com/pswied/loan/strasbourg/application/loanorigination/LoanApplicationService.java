package com.pswied.loan.strasbourg.application.loanorigination;

import com.pswied.loan.strasbourg.application.merchantidentity.MerchantIdentityValidationService;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanApplicationStatus;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanApplicationSubmissionRequest;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanApplicationSubmissionResult;
import com.pswied.loan.strasbourg.domain.merchantidentity.MerchantIdentityValidationRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class LoanApplicationService {

    private final ApplicantVerificationPort applicantVerificationPort;
    private final MerchantIdentityValidationService merchantIdentityValidationService;

    @Inject
    public LoanApplicationService(
            ApplicantVerificationPort applicantVerificationPort,
            MerchantIdentityValidationService merchantIdentityValidationService
    ) {
        this.applicantVerificationPort = applicantVerificationPort;
        this.merchantIdentityValidationService = merchantIdentityValidationService;
    }

    public LoanApplicationSubmissionResult submit(LoanApplicationSubmissionRequest request) {
        Instant now = Instant.now();
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

        return new LoanApplicationSubmissionResult(
                UUID.randomUUID().toString(),
                request.applicantName().trim(),
                request.merchantId().trim(),
                request.amount(),
                request.tenorMonths(),
                LoanApplicationStatus.SUBMITTED,
                applicantVerification.status(),
                applicantVerification.reason(),
                merchantVerification.status(),
                merchantVerification.reason(),
                merchantVerification.sourceSystem(),
                merchantVerification.externalReference(),
                merchantVerification.validatedAt(),
                now
        );
    }
}
