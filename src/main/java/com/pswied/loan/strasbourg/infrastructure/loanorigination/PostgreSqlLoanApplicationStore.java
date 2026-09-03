package com.pswied.loan.strasbourg.infrastructure.loanorigination;

import com.pswied.loan.strasbourg.application.loanorigination.LoanApplicationStorePort;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanApplication;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.Optional;

@ApplicationScoped
public class PostgreSqlLoanApplicationStore implements LoanApplicationStorePort, PanacheRepositoryBase<LoanApplicationEntity, String> {

    @Override
    @Transactional
    public void save(LoanApplication loanApplication) {
        LoanApplicationEntity managed = findById(loanApplication.loanApplicationId());
        if (managed == null) {
            persist(LoanApplicationEntity.fromDomain(loanApplication));
            return;
        }
        managed.applicantName = loanApplication.applicantName();
        managed.merchantId = loanApplication.merchantId();
        managed.amount = loanApplication.amount();
        managed.tenorMonths = loanApplication.tenorMonths();
        managed.status = loanApplication.status();
        managed.lifecycleStage = loanApplication.lifecycleStage();
        managed.decision = loanApplication.decision();
        managed.decisionReasonCode = loanApplication.decisionReasonCode();
        managed.eligibilityStatus = loanApplication.eligibilityStatus();
        managed.eligibilityReason = loanApplication.eligibilityReason();
        managed.applicantVerificationStatus = loanApplication.applicantVerificationStatus();
        managed.applicantVerificationReason = loanApplication.applicantVerificationReason();
        managed.merchantVerificationStatus = loanApplication.merchantVerificationStatus();
        managed.merchantVerificationReason = loanApplication.merchantVerificationReason();
        managed.merchantVerificationSourceSystem = loanApplication.merchantVerificationSourceSystem();
        managed.merchantVerificationReference = loanApplication.merchantVerificationReference();
        managed.submittedAt = loanApplication.submittedAt();
        managed.verifiedAt = loanApplication.verifiedAt();
        managed.decidedAt = loanApplication.decidedAt();
    }

    @Override
    public Optional<LoanApplication> findByLoanApplicationId(String loanApplicationId) {
        return findByIdOptional(loanApplicationId).map(LoanApplicationEntity::toDomain);
    }
}
