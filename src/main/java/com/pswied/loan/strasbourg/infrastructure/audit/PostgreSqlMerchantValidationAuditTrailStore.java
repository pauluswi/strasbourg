package com.pswied.loan.strasbourg.infrastructure.audit;

import com.pswied.loan.strasbourg.application.audit.MerchantValidationAuditTrailStorePort;
import com.pswied.loan.strasbourg.domain.audit.MerchantValidationAuditTrailEntry;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class PostgreSqlMerchantValidationAuditTrailStore implements MerchantValidationAuditTrailStorePort,
        PanacheRepositoryBase<MerchantValidationAuditTrailEntity, String> {

    @Override
    @Transactional
    public void save(MerchantValidationAuditTrailEntry entry) {
        persist(MerchantValidationAuditTrailEntity.fromDomain(entry));
    }

    @Override
    public List<MerchantValidationAuditTrailEntry> findByIdempotencyKey(String idempotencyKey) {
        return find("idempotencyKey = ?1 order by createdAt", idempotencyKey)
                .stream()
                .map(MerchantValidationAuditTrailEntity.class::cast)
                .map(MerchantValidationAuditTrailEntity::toDomain)
                .toList();
    }
}
