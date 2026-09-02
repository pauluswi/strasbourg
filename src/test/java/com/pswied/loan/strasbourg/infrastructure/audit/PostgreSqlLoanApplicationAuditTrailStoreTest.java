package com.pswied.loan.strasbourg.infrastructure.audit;

import com.pswied.loan.strasbourg.application.audit.LoanApplicationAuditTrailStorePort;
import com.pswied.loan.strasbourg.domain.audit.LoanApplicationAuditTrailEntry;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class PostgreSqlLoanApplicationAuditTrailStoreTest {

    @Inject
    LoanApplicationAuditTrailStorePort store;

    @Test
    void storesLifecycleAuditEntries() {
        String loanApplicationId = "loan-audit-1001";
        store.save(new LoanApplicationAuditTrailEntry(
                UUID.randomUUID().toString(),
                loanApplicationId,
                "SUBMITTED",
                "LoanApplicationSubmitted",
                "{\"loanApplicationId\":\"loan-audit-1001\",\"lifecycleStage\":\"SUBMITTED\"}",
                Instant.parse("2026-09-03T01:10:00Z")
        ));
        store.save(new LoanApplicationAuditTrailEntry(
                UUID.randomUUID().toString(),
                loanApplicationId,
                "DECIDED",
                "LoanApplicationDecided",
                "{\"loanApplicationId\":\"loan-audit-1001\",\"lifecycleStage\":\"DECIDED\"}",
                Instant.parse("2026-09-03T01:10:03Z")
        ));

        var entries = store.findByLoanApplicationId(loanApplicationId);
        assertThat(entries).hasSize(2);
        assertThat(entries.getFirst().eventType()).isEqualTo("LoanApplicationSubmitted");
        assertThat(entries.getLast().eventType()).isEqualTo("LoanApplicationDecided");
    }
}
