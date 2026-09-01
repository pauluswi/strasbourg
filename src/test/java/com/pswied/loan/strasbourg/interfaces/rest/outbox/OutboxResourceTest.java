package com.pswied.loan.strasbourg.interfaces.rest.outbox;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

@QuarkusTest
class OutboxResourceTest {

    @Test
    void publishesPendingEventsFromOutboxEndpoint() {
        given()
                .header("Idempotency-Key", "idem-outbox-4001")
                .contentType("application/json")
                .body("""
                        {
                          "merchantId": "m-4001",
                          "legalName": "Acme Trading",
                          "taxNumber": "TAX-4001"
                        }
                        """)
                .when()
                .post("/api/merchant-identities/validate")
                .then()
                .statusCode(200);

        given()
                .when()
                .post("/api/outbox/publish")
                .then()
                .statusCode(200)
                .body("publishedEvents", greaterThanOrEqualTo(1))
                .body("pendingEvents", equalTo(0));
    }
}
