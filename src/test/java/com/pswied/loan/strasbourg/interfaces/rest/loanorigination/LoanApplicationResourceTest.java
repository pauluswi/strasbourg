package com.pswied.loan.strasbourg.interfaces.rest.loanorigination;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class LoanApplicationResourceTest {

    @Test
    void createsLoanApplication() {
        given()
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
                .body("status", equalTo("SUBMITTED"))
                .body("applicantVerificationStatus", equalTo("PASSED"))
                .body("applicantVerificationReason", equalTo("Applicant passed mock verification"))
                .body("merchantVerificationStatus", equalTo("VERIFIED"))
                .body("merchantVerificationSourceSystem", equalTo("SAP_S4"))
                .body("merchantVerificationReference", notNullValue())
                .body("verifiedAt", notNullValue())
                .body("submittedAt", notNullValue());
    }
}
