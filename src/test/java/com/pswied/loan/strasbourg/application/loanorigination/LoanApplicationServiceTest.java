package com.pswied.loan.strasbourg.application.loanorigination;

import com.pswied.loan.strasbourg.domain.loanorigination.LoanApplicationStatus;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanApplicationSubmissionRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class LoanApplicationServiceTest {

    @Test
    void submitsLoanApplicationWithSubmittedStatus() {
        LoanApplicationService service = new LoanApplicationService();

        var result = service.submit(new LoanApplicationSubmissionRequest(
                "Alice Applicant",
                "m-4001",
                "Acme Merchant",
                "TAX-4001",
                new BigDecimal("15000.00"),
                24
        ));

        assertThat(result.loanApplicationId()).isNotBlank();
        assertThat(result.applicantName()).isEqualTo("Alice Applicant");
        assertThat(result.merchantId()).isEqualTo("m-4001");
        assertThat(result.amount()).isEqualByComparingTo("15000.00");
        assertThat(result.tenorMonths()).isEqualTo(24);
        assertThat(result.status()).isEqualTo(LoanApplicationStatus.SUBMITTED);
        assertThat(result.submittedAt()).isNotNull();
    }
}
