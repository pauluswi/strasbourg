package com.pswied.loan.strasbourg.application.loanorigination;

import com.pswied.loan.strasbourg.domain.loanorigination.FraudAssessmentResult;

import java.math.BigDecimal;

public interface FraudAssessmentPort {
    FraudAssessmentResult assess(String applicantName, String merchantId, BigDecimal amount, int tenorMonths);
}
