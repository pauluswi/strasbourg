package com.pswied.loan.strasbourg.interfaces.rest.merchantidentity;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

@QuarkusTest
class MerchantIdentityResourceTest {

    @Test
    void validatesMerchantIdentityThroughTheMockAcl() {
        given()
                .header("Idempotency-Key", "idem-merchant-2001")
                .contentType("application/json")
                .body("""
                        {
                          "merchantId": "m-2001",
                          "legalName": "Acme Trading",
                          "taxNumber": "TAX-2001"
                        }
                        """)
                .when()
                .post("/api/merchant-identities/validate")
                .then()
                .statusCode(200)
                .body("merchantId", equalTo("m-2001"))
                .body("status", equalTo("VERIFIED"))
                .body("sourceSystem", equalTo("SAP_S4"))
                .body("externalReference", startsWith("SAP-S4-VER-"));
    }

    @Test
    void replaysTheSameResultWhenIdempotencyKeyIsReused() {
        String firstValidatedAt = given()
                .header("Idempotency-Key", "idem-merchant-2002")
                .contentType("application/json")
                .body("""
                        {
                          "merchantId": "m-2002",
                          "legalName": "Acme Replay",
                          "taxNumber": "TAX-2002"
                        }
                        """)
                .when()
                .post("/api/merchant-identities/validate")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getString("validatedAt");

        given()
                .header("Idempotency-Key", "idem-merchant-2002")
                .contentType("application/json")
                .body("""
                        {
                          "merchantId": "m-2002",
                          "legalName": "Acme Replay",
                          "taxNumber": "TAX-2002"
                        }
                        """)
                .when()
                .post("/api/merchant-identities/validate")
                .then()
                .statusCode(200)
                .body("validatedAt", equalTo(firstValidatedAt));
    }

    @Test
    void rejectsReusingTheSameIdempotencyKeyForDifferentRequest() {
        given()
                .header("Idempotency-Key", "idem-merchant-2003")
                .contentType("application/json")
                .body("""
                        {
                          "merchantId": "m-2003",
                          "legalName": "Acme First",
                          "taxNumber": "TAX-2003"
                        }
                        """)
                .when()
                .post("/api/merchant-identities/validate")
                .then()
                .statusCode(200);

        given()
                .header("Idempotency-Key", "idem-merchant-2003")
                .contentType("application/json")
                .body("""
                        {
                          "merchantId": "m-2003",
                          "legalName": "Acme Changed",
                          "taxNumber": "TAX-2003"
                        }
                        """)
                .when()
                .post("/api/merchant-identities/validate")
                .then()
                .statusCode(409);
    }
}
