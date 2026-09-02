package com.pswied.loan.strasbourg.application.loanorigination;

import com.pswied.loan.strasbourg.domain.loanorigination.LoanApplication;

import java.util.Optional;

public interface LoanApplicationStorePort {
    void save(LoanApplication loanApplication);

    Optional<LoanApplication> findByLoanApplicationId(String loanApplicationId);
}
