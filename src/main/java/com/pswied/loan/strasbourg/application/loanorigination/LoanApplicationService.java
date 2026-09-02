package com.pswied.loan.strasbourg.application.loanorigination;

import com.pswied.loan.strasbourg.domain.loanorigination.LoanApplicationStatus;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanApplicationSubmissionRequest;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanApplicationSubmissionResult;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class LoanApplicationService {

    public LoanApplicationSubmissionResult submit(LoanApplicationSubmissionRequest request) {
        return new LoanApplicationSubmissionResult(
                UUID.randomUUID().toString(),
                request.applicantName().trim(),
                request.merchantId().trim(),
                request.amount(),
                request.tenorMonths(),
                LoanApplicationStatus.SUBMITTED,
                Instant.now()
        );
    }
}
