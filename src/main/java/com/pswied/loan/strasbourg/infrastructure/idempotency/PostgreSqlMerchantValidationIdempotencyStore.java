package com.pswied.loan.strasbourg.infrastructure.idempotency;

import com.pswied.loan.strasbourg.application.idempotency.IdempotencyConflictException;
import com.pswied.loan.strasbourg.application.idempotency.MerchantValidationIdempotencyStorePort;
import com.pswied.loan.strasbourg.domain.idempotency.MerchantValidationIdempotencyEntry;
import com.pswied.loan.strasbourg.domain.merchantidentity.MerchantIdentityValidationResult;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.Optional;

@ApplicationScoped
public class PostgreSqlMerchantValidationIdempotencyStore implements MerchantValidationIdempotencyStorePort,
        PanacheRepositoryBase<MerchantValidationIdempotencyEntity, String> {

    @Override
    public Optional<MerchantValidationIdempotencyEntry> findByKey(String idempotencyKey) {
        return findByIdOptional(idempotencyKey).map(MerchantValidationIdempotencyEntity::toDomain);
    }

    @Override
    @Transactional
    public void save(String idempotencyKey, String requestFingerprint, MerchantIdentityValidationResult result) {
        MerchantValidationIdempotencyEntity existing = findById(idempotencyKey);
        if (existing == null) {
            persist(MerchantValidationIdempotencyEntity.fromDomain(idempotencyKey, requestFingerprint, result));
            return;
        }

        if (!existing.requestFingerprint.equals(requestFingerprint)) {
            throw new IdempotencyConflictException("Idempotency key already used for a different request");
        }
    }
}
