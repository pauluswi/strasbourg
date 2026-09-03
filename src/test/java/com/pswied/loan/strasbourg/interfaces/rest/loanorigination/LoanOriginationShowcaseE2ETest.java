package com.pswied.loan.strasbourg.interfaces.rest.loanorigination;

import com.pswied.loan.strasbourg.application.audit.LoanApplicationAuditTrailStorePort;
import com.pswied.loan.strasbourg.application.outbox.OutboxEventStorePort;
import com.pswied.loan.strasbourg.domain.outbox.OutboxEvent;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class LoanOriginationShowcaseE2ETest {

    @Inject
    LoanApplicationAuditTrailStorePort loanApplicationAuditTrailStore;

    @Inject
    OutboxEventStorePort outboxEventStore;

    @Test
    void submitsLoanApplicationAndPersistsDecisionAuditAndOutbox() {
        String merchantId = "m-showcase-6001";

        String loanApplicationId = given()
                .contentType("application/json")
                .body("""
                        {
                          "applicantName": "Charlie Owner",
                          "merchantId": "m-showcase-6001",
                          "merchantLegalName": "Showcase Merchant",
                          "merchantTaxNumber": "TAX-6001",
                          "amount": 30000.00,
                          "tenorMonths": 36
                        }
                        """)
                .when()
                .post("/api/loan-applications")
                .then()
                .statusCode(200)
                .body("loanApplicationId", notNullValue())
                .body("merchantVerificationStatus", equalTo("VERIFIED"))
                .body("eligibilityStatus", equalTo("ELIGIBLE"))
                .body("creditAssessmentStatus", equalTo("PASSED"))
                .body("decision", equalTo("APPROVED"))
                .extract()
                .jsonPath()
                .getString("loanApplicationId");

        given()
                .when()
                .get("/api/loan-applications/{id}", loanApplicationId)
                .then()
                .statusCode(200)
                .body("loanApplicationId", equalTo(loanApplicationId))
                .body("eligibilityStatus", equalTo("ELIGIBLE"))
                .body("creditAssessmentStatus", equalTo("PASSED"))
                .body("merchantVerificationStatus", equalTo("VERIFIED"))
                .body("journey", hasSize(3));

        assertThat(loanApplicationAuditTrailStore.findByLoanApplicationId(loanApplicationId))
                .extracting(entry -> entry.eventType())
                .containsExactly("LoanApplicationSubmitted", "LoanApplicationVerified", "LoanApplicationDecided");

        var allPendingEvents = outboxEventStore.findPendingEvents();
        assertThat(allPendingEvents.stream()
                .filter(event -> event.aggregateId().equals(loanApplicationId))
                .map(OutboxEvent::eventType)
                .toList())
                .containsExactly("LoanApplicationSubmitted", "LoanApplicationVerified", "LoanApplicationDecided");
        assertThat(allPendingEvents.stream()
                .filter(event -> event.aggregateId().equals(merchantId))
                .map(OutboxEvent::eventType)
                .toList())
                .contains("MerchantIdentityValidated");
    }
}
