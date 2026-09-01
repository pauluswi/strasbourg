package com.pswied.loan.strasbourg.application.audit;

import com.pswied.loan.strasbourg.domain.audit.MerchantValidationAuditTrailEntry;

import java.util.List;

public interface MerchantValidationAuditTrailStorePort {
    void save(MerchantValidationAuditTrailEntry entry);

    List<MerchantValidationAuditTrailEntry> findByIdempotencyKey(String idempotencyKey);
}
