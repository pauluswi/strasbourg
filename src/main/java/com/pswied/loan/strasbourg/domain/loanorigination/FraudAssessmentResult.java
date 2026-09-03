package com.pswied.loan.strasbourg.domain.loanorigination;

public record FraudAssessmentResult(
        FraudAssessmentStatus status,
        String reason
) {
}
