package com.pswied.loan.strasbourg.infrastructure.loanorigination;

import com.pswied.loan.strasbourg.application.loanorigination.EligibilityAssessmentPort;
import com.pswied.loan.strasbourg.domain.loanorigination.EligibilityAssessmentResult;
import com.pswied.loan.strasbourg.domain.loanorigination.EligibilityAssessmentStatus;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;

@ApplicationScoped
public class MockEligibilityAssessmentAdapter implements EligibilityAssessmentPort {

    private static final BigDecimal MIN_AMOUNT = new BigDecimal("1000");
    private static final BigDecimal AUTO_ELIGIBLE_MAX_AMOUNT = new BigDecimal("50000");

    @Override
    public EligibilityAssessmentResult assess(BigDecimal amount, int tenorMonths) {
        if (amount.compareTo(MIN_AMOUNT) < 0 || tenorMonths < 6) {
            return new EligibilityAssessmentResult(
                    EligibilityAssessmentStatus.INELIGIBLE,
                    "Application does not meet minimum amount or tenor policy"
            );
        }

        if (amount.compareTo(AUTO_ELIGIBLE_MAX_AMOUNT) > 0 || tenorMonths > 48) {
            return new EligibilityAssessmentResult(
                    EligibilityAssessmentStatus.MANUAL_REVIEW,
                    "Application exceeds straight-through eligibility threshold"
            );
        }

        return new EligibilityAssessmentResult(
                EligibilityAssessmentStatus.ELIGIBLE,
                "Application passed initial eligibility policy"
        );
    }
}
