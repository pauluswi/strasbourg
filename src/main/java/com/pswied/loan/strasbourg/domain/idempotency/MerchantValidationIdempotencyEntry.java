package com.pswied.loan.strasbourg.domain.idempotency;

import com.pswied.loan.strasbourg.domain.merchantidentity.MerchantIdentityValidationResult;

public record MerchantValidationIdempotencyEntry(
        String idempotencyKey,
        String requestFingerprint,
        MerchantIdentityValidationResult result
) {
}
