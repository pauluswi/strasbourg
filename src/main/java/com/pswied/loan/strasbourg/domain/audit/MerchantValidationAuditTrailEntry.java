package com.pswied.loan.strasbourg.domain.audit;

import java.time.Instant;

public record MerchantValidationAuditTrailEntry(
        String auditId,
        String idempotencyKey,
        String merchantId,
        String requestPayload,
        String mappedSapResponse,
        String decisionStatus,
        String decisionReason,
        boolean replayed,
        Instant createdAt
) {
}
