package com.pswied.loan.strasbourg.application.audit;

import com.pswied.loan.strasbourg.domain.audit.LoanApplicationAuditTrailEntry;

import java.util.List;

public interface LoanApplicationAuditTrailStorePort {
    void save(LoanApplicationAuditTrailEntry entry);

    List<LoanApplicationAuditTrailEntry> findByLoanApplicationId(String loanApplicationId);
}
