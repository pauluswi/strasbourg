package com.pswied.loan.strasbourg.application.idempotency;

import com.pswied.loan.strasbourg.domain.idempotency.MerchantValidationIdempotencyEntry;
import com.pswied.loan.strasbourg.domain.merchantidentity.MerchantIdentityValidationResult;

import java.util.Optional;

public interface MerchantValidationIdempotencyStorePort {
    Optional<MerchantValidationIdempotencyEntry> findByKey(String idempotencyKey);

    void save(String idempotencyKey, String requestFingerprint, MerchantIdentityValidationResult result);
}
