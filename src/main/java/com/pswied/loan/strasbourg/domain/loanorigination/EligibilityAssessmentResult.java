package com.pswied.loan.strasbourg.domain.loanorigination;

public record EligibilityAssessmentResult(
        EligibilityAssessmentStatus status,
        String reason
) {
}
