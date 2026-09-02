package com.pswied.loan.strasbourg.domain.loanorigination;

import com.pswied.loan.strasbourg.domain.merchantidentity.MerchantIdentityStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record LoanApplication(
        String loanApplicationId,
        String applicantName,
        String merchantId,
        BigDecimal amount,
        int tenorMonths,
        LoanApplicationStatus status,
        LoanApplicationLifecycleStage lifecycleStage,
        LoanOriginationDecision decision,
        LoanOriginationDecisionReasonCode decisionReasonCode,
        ApplicantVerificationStatus applicantVerificationStatus,
        String applicantVerificationReason,
        MerchantIdentityStatus merchantVerificationStatus,
        String merchantVerificationReason,
        String merchantVerificationSourceSystem,
        String merchantVerificationReference,
        Instant submittedAt,
        Instant verifiedAt,
        Instant decidedAt
) {
}
