package com.pswied.loan.strasbourg.infrastructure.loanorigination;

import com.pswied.loan.strasbourg.application.loanorigination.CreditAssessmentPort;
import com.pswied.loan.strasbourg.domain.loanorigination.CreditAssessmentResult;
import com.pswied.loan.strasbourg.domain.loanorigination.CreditAssessmentStatus;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.util.Locale;

@ApplicationScoped
public class MockCreditAssessmentAdapter implements CreditAssessmentPort {

    @Override
    public CreditAssessmentResult assess(String applicantName, BigDecimal amount, int tenorMonths) {
        String normalizedName = applicantName == null ? "" : applicantName.trim().toLowerCase(Locale.ROOT);

        if (normalizedName.contains("default") || normalizedName.contains("delinquent")) {
            return new CreditAssessmentResult(
                    CreditAssessmentStatus.REJECTED,
                    "Applicant failed mock credit policy"
            );
        }

        if (amount.compareTo(new BigDecimal("75000")) > 0 || tenorMonths > 72) {
            return new CreditAssessmentResult(
                    CreditAssessmentStatus.MANUAL_REVIEW,
                    "Application requires manual credit analyst review"
            );
        }

        return new CreditAssessmentResult(
                CreditAssessmentStatus.PASSED,
                "Application passed mock credit assessment"
        );
    }
}
