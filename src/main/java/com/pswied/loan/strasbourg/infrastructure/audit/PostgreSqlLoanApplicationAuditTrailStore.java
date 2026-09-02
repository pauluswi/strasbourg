package com.pswied.loan.strasbourg.infrastructure.audit;

import com.pswied.loan.strasbourg.application.audit.LoanApplicationAuditTrailStorePort;
import com.pswied.loan.strasbourg.domain.audit.LoanApplicationAuditTrailEntry;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class PostgreSqlLoanApplicationAuditTrailStore implements LoanApplicationAuditTrailStorePort,
        PanacheRepositoryBase<LoanApplicationAuditTrailEntity, String> {

    @Override
    @Transactional
    public void save(LoanApplicationAuditTrailEntry entry) {
        persist(LoanApplicationAuditTrailEntity.fromDomain(entry));
    }

    @Override
    public List<LoanApplicationAuditTrailEntry> findByLoanApplicationId(String loanApplicationId) {
        return find("loanApplicationId = ?1 order by createdAt", loanApplicationId)
                .stream()
                .map(LoanApplicationAuditTrailEntity.class::cast)
                .map(LoanApplicationAuditTrailEntity::toDomain)
                .toList();
    }
}
