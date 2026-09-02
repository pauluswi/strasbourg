package com.pswied.loan.strasbourg.domain.loanorigination;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record LoanApplicationSubmissionRequest(
        @NotBlank String applicantName,
        @NotBlank String merchantId,
        @NotBlank String merchantLegalName,
        @NotBlank String merchantTaxNumber,
        @Positive BigDecimal amount,
        @Positive int tenorMonths
) {
}
