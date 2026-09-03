package com.pswied.loan.strasbourg.domain.loanorigination;

public record CreditAssessmentResult(
        CreditAssessmentStatus status,
        String reason
) {
}
