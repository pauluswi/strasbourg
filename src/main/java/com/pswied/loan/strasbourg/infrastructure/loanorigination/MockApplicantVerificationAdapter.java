package com.pswied.loan.strasbourg.infrastructure.loanorigination;

import com.pswied.loan.strasbourg.application.loanorigination.ApplicantVerificationPort;
import com.pswied.loan.strasbourg.domain.loanorigination.ApplicantVerificationResult;
import com.pswied.loan.strasbourg.domain.loanorigination.ApplicantVerificationStatus;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.util.Locale;

@ApplicationScoped
public class MockApplicantVerificationAdapter implements ApplicantVerificationPort {

    @Override
    public ApplicantVerificationResult verify(String applicantName, BigDecimal amount, int tenorMonths) {
        String normalizedName = applicantName == null ? "" : applicantName.trim().toLowerCase(Locale.ROOT);

        if (normalizedName.contains("fraud") || normalizedName.contains("blacklist")) {
            return new ApplicantVerificationResult(
                    ApplicantVerificationStatus.REJECTED,
                    "Applicant was flagged by mock screening"
            );
        }

        if (amount.compareTo(new BigDecimal("100000")) > 0 || tenorMonths > 60) {
            return new ApplicantVerificationResult(
                    ApplicantVerificationStatus.MANUAL_REVIEW,
                    "Applicant requires manual affordability review"
            );
        }

        return new ApplicantVerificationResult(
                ApplicantVerificationStatus.PASSED,
                "Applicant passed mock verification"
        );
    }
}
