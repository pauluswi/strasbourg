package com.pswied.loan.strasbourg.application.loanorigination;

import com.pswied.loan.strasbourg.domain.loanorigination.EligibilityAssessmentResult;

import java.math.BigDecimal;

public interface EligibilityAssessmentPort {
    EligibilityAssessmentResult assess(BigDecimal amount, int tenorMonths);
}
