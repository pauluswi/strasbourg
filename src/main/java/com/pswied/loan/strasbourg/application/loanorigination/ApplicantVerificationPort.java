package com.pswied.loan.strasbourg.application.loanorigination;

import com.pswied.loan.strasbourg.domain.loanorigination.ApplicantVerificationResult;

import java.math.BigDecimal;

public interface ApplicantVerificationPort {
    ApplicantVerificationResult verify(String applicantName, BigDecimal amount, int tenorMonths);
}
