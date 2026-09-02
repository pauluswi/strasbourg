package com.pswied.loan.strasbourg.domain.loanorigination;

import java.math.BigDecimal;
import java.time.Instant;

public record LoanApplicationSubmissionResult(
        String loanApplicationId,
        String applicantName,
        String merchantId,
        BigDecimal amount,
        int tenorMonths,
        LoanApplicationStatus status,
        Instant submittedAt
) {
}
