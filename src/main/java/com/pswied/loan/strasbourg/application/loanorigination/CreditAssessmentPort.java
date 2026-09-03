package com.pswied.loan.strasbourg.application.loanorigination;

import com.pswied.loan.strasbourg.domain.loanorigination.CreditAssessmentResult;

import java.math.BigDecimal;

public interface CreditAssessmentPort {
    CreditAssessmentResult assess(String applicantName, BigDecimal amount, int tenorMonths);
}
