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
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

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
                .body("eligibilityStatus", equalTo("ELIGIBLE"))
                .body("eligibilityReason", equalTo("Application passed initial eligibility policy"))
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
        assertThat(persisted.orElseThrow().eligibilityStatus().name()).isEqualTo("ELIGIBLE");
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

    @Test
    void getsLoanApplicationJourneyById() {
        String loanApplicationId = given()
                .contentType("application/json")
                .body("""
                        {
                          "applicantName": "Bob Owner",
                          "merchantId": "m-5002",
                          "merchantLegalName": "Beta Merchant",
                          "merchantTaxNumber": "TAX-5002",
                          "amount": 21000.00,
                          "tenorMonths": 36
                        }
                        """)
                .when()
                .post("/api/loan-applications")
                .then()
                .statusCode(200)
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
                .body("merchantVerificationStatus", equalTo("VERIFIED"))
                .body("merchantVerificationSourceSystem", equalTo("SAP_S4"))
                .body("merchantVerificationReference", notNullValue())
                .body("journey", hasSize(3))
                .body("journey[0].eventType", equalTo("LoanApplicationSubmitted"))
                .body("journey[1].eventType", equalTo("LoanApplicationVerified"))
                .body("journey[2].eventType", equalTo("LoanApplicationDecided"));
    }

    @Test
    void returnsNotFoundForUnknownLoanApplication() {
        given()
                .when()
                .get("/api/loan-applications/{id}", "loan-missing-9999")
                .then()
                .statusCode(404);
    }

    @Test
    void routesToManualReviewWhenEligibilityThresholdIsExceeded() {
        given()
                .contentType("application/json")
                .body("""
                        {
                          "applicantName": "Dana Owner",
                          "merchantId": "m-5003",
                          "merchantLegalName": "Gamma Merchant",
                          "merchantTaxNumber": "TAX-5003",
                          "amount": 60000.00,
                          "tenorMonths": 24
                        }
                        """)
                .when()
                .post("/api/loan-applications")
                .then()
                .statusCode(200)
                .body("decision", equalTo("MANUAL_REVIEW"))
                .body("decisionReasonCode", equalTo("ELIGIBILITY_MANUAL_REVIEW_REQUIRED"))
                .body("eligibilityStatus", equalTo("MANUAL_REVIEW"));
    }
}
