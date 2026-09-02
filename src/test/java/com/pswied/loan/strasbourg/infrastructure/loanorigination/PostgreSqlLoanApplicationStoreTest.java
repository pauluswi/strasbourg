package com.pswied.loan.strasbourg.infrastructure.loanorigination;

import com.pswied.loan.strasbourg.application.loanorigination.LoanApplicationStorePort;
import com.pswied.loan.strasbourg.domain.loanorigination.ApplicantVerificationStatus;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanApplication;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanApplicationLifecycleStage;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanApplicationStatus;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanOriginationDecision;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanOriginationDecisionReasonCode;
import com.pswied.loan.strasbourg.domain.merchantidentity.MerchantIdentityStatus;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class PostgreSqlLoanApplicationStoreTest {

    @Inject
    LoanApplicationStorePort store;

    @Test
    void persistsLoanApplicationLifecycle() {
        LoanApplication loanApplication = new LoanApplication(
                "loan-store-1001",
                "Alice Applicant",
                "m-store-1001",
                new BigDecimal("12000.00"),
                24,
                LoanApplicationStatus.DECIDED,
                LoanApplicationLifecycleStage.DECIDED,
                LoanOriginationDecision.APPROVED,
                LoanOriginationDecisionReasonCode.ALL_CHECKS_PASSED,
                ApplicantVerificationStatus.PASSED,
                "Applicant passed mock verification",
                MerchantIdentityStatus.VERIFIED,
                "SAP S/4 confirmed the merchant identity",
                "SAP_S4",
                "SAP-S4-VER-m-store-1001",
                Instant.parse("2026-09-03T01:00:00Z"),
                Instant.parse("2026-09-03T01:00:10Z"),
                Instant.parse("2026-09-03T01:00:12Z")
        );

        store.save(loanApplication);

        var persisted = store.findByLoanApplicationId("loan-store-1001");
        assertThat(persisted).isPresent();
        assertThat(persisted.orElseThrow().lifecycleStage()).isEqualTo(LoanApplicationLifecycleStage.DECIDED);
        assertThat(persisted.orElseThrow().decision()).isEqualTo(LoanOriginationDecision.APPROVED);
        assertThat(persisted.orElseThrow().decidedAt()).isEqualTo(Instant.parse("2026-09-03T01:00:12Z"));
    }
}
