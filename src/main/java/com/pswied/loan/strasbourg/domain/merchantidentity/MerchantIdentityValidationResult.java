package com.pswied.loan.strasbourg.domain.merchantidentity;

import java.time.Instant;

public record MerchantIdentityValidationResult(
        String merchantId,
        MerchantIdentityStatus status,
        String reason,
        String sourceSystem,
        String externalReference,
        Instant validatedAt
) {
}
