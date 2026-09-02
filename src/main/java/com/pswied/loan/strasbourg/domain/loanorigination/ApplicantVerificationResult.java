package com.pswied.loan.strasbourg.domain.loanorigination;

public record ApplicantVerificationResult(
        ApplicantVerificationStatus status,
        String reason
) {
}
