package com.pswied.loan.strasbourg.interfaces.rest.loanorigination;

import com.pswied.loan.strasbourg.application.audit.LoanApplicationAuditTrailStorePort;
import com.pswied.loan.strasbourg.application.loanorigination.LoanApplicationStorePort;
import com.pswied.loan.strasbourg.application.outbox.OutboxEventStorePort;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanApplicationLifecycleStage;
import com.pswied.loan.strasbourg.domain.outbox.OutboxEvent;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class LoanApplicationResourceTest {

    @Inject
    LoanApplicationStorePort loanApplicationStore;

    @Inject
    LoanApplicationAuditTrailStorePort loanApplicationAuditTrailStore;

    @Inject
    OutboxEventStorePort outboxEventStore;

    @Test
    void createsLoanApplication() {
        String loanApplicationId = given()
                .contentType("application/json")
                .body("""
                        {
                          "applicantName": "Alice Applicant",
                          "merchantId": "m-5001",
                          "merchantLegalName": "Acme Merchant",
                          "merchantTaxNumber": "TAX-5001",
                          "amount": 15000.00,
                          "tenorMonths": 24
                        }
                        """)
                .when()
                .post("/api/loan-applications")
                .then()
                .statusCode(200)
                .body("loanApplicationId", notNullValue())
                .body("applicantName", equalTo("Alice Applicant"))
                .body("merchantId", equalTo("m-5001"))
                .body("amount", equalTo(15000.0F))
                .body("tenorMonths", equalTo(24))
                .body("status", equalTo("DECIDED"))
                .body("decision", equalTo("APPROVED"))
                .body("decisionReasonCode", equalTo("ALL_CHECKS_PASSED"))
                .body("applicantVerificationStatus", equalTo("PASSED"))
                .body("applicantVerificationReason", equalTo("Applicant passed mock verification"))
                .body("merchantVerificationStatus", equalTo("VERIFIED"))
                .body("merchantVerificationSourceSystem", equalTo("SAP_S4"))
                .body("merchantVerificationReference", notNullValue())
                .body("verifiedAt", notNullValue())
                .body("submittedAt", notNullValue())
                .extract()
                .jsonPath()
                .getString("loanApplicationId");

        var persisted = loanApplicationStore.findByLoanApplicationId(loanApplicationId);
        assertThat(persisted).isPresent();
        assertThat(persisted.orElseThrow().lifecycleStage()).isEqualTo(LoanApplicationLifecycleStage.DECIDED);
        assertThat(persisted.orElseThrow().decidedAt()).isNotNull();

        var auditEntries = loanApplicationAuditTrailStore.findByLoanApplicationId(loanApplicationId);
        assertThat(auditEntries).hasSize(3);
        assertThat(auditEntries)
                .extracting(entry -> entry.eventType())
                .containsExactly("LoanApplicationSubmitted", "LoanApplicationVerified", "LoanApplicationDecided");

        assertThat(outboxEventStore.findPendingEvents().stream()
                .filter(event -> event.aggregateId().equals(loanApplicationId))
                .map(OutboxEvent::eventType)
                .toList())
                .containsExactly("LoanApplicationSubmitted", "LoanApplicationVerified", "LoanApplicationDecided");
    }
}
