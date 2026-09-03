package com.pswied.loan.strasbourg.infrastructure.loanorigination;

import com.pswied.loan.strasbourg.application.loanorigination.FraudAssessmentPort;
import com.pswied.loan.strasbourg.domain.loanorigination.FraudAssessmentResult;
import com.pswied.loan.strasbourg.domain.loanorigination.FraudAssessmentStatus;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.util.Locale;

@ApplicationScoped
public class MockFraudAssessmentAdapter implements FraudAssessmentPort {

    @Override
    public FraudAssessmentResult assess(String applicantName, String merchantId, BigDecimal amount, int tenorMonths) {
        String normalizedMerchantId = merchantId == null ? "" : merchantId.trim().toLowerCase(Locale.ROOT);

        if (normalizedMerchantId.contains("watchlist") || normalizedMerchantId.contains("stolen")) {
            return new FraudAssessmentResult(
                    FraudAssessmentStatus.REJECTED,
                    "Application failed mock fraud policy"
            );
        }

        if (amount.compareTo(new BigDecimal("90000")) > 0 || tenorMonths > 84) {
            return new FraudAssessmentResult(
                    FraudAssessmentStatus.MANUAL_REVIEW,
                    "Application requires manual fraud analyst review"
            );
        }

        return new FraudAssessmentResult(
                FraudAssessmentStatus.PASSED,
                "Application passed mock fraud assessment"
        );
    }
}
