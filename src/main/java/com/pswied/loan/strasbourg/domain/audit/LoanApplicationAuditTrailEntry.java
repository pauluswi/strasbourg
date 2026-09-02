package com.pswied.loan.strasbourg.domain.audit;

import java.time.Instant;

public record LoanApplicationAuditTrailEntry(
        String auditId,
        String loanApplicationId,
        String lifecycleStage,
        String eventType,
        String payload,
        Instant createdAt
) {
}
