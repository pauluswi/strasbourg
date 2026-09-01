package com.pswied.loan.strasbourg.infrastructure.audit;

import com.pswied.loan.strasbourg.application.audit.MerchantValidationAuditTrailStorePort;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class PostgreSqlMerchantValidationAuditTrailStoreTest {

    @Inject
    MerchantValidationAuditTrailStorePort auditTrailStore;

    @Test
    void storesRequestMappedResponseAndDecisionReason() {
        given()
                .header("Idempotency-Key", "idem-audit-5001")
                .contentType("application/json")
                .body("""
                        {
                          "merchantId": "m-5001",
                          "legalName": "Acme Audit",
                          "taxNumber": "TAX-5001"
                        }
                        """)
                .when()
                .post("/api/merchant-identities/validate")
                .then()
                .statusCode(200);

        var entries = auditTrailStore.findByIdempotencyKey("idem-audit-5001");
        assertThat(entries).hasSize(1);
        var entry = entries.getFirst();
        assertThat(entry.requestPayload()).contains("\"merchantId\":\"m-5001\"");
        assertThat(entry.mappedSapResponse()).contains("\"sourceSystem\":\"SAP_S4\"");
        assertThat(entry.decisionReason()).isNotBlank();
        assertThat(entry.replayed()).isFalse();
    }

    @Test
    void storesReplayFlagWhenReturningIdempotentResponse() {
        given()
                .header("Idempotency-Key", "idem-audit-5002")
                .contentType("application/json")
                .body("""
                        {
                          "merchantId": "m-5002",
                          "legalName": "Acme Replay Audit",
                          "taxNumber": "TAX-5002"
                        }
                        """)
                .when()
                .post("/api/merchant-identities/validate")
                .then()
                .statusCode(200);

        given()
                .header("Idempotency-Key", "idem-audit-5002")
                .contentType("application/json")
                .body("""
                        {
                          "merchantId": "m-5002",
                          "legalName": "Acme Replay Audit",
                          "taxNumber": "TAX-5002"
                        }
                        """)
                .when()
                .post("/api/merchant-identities/validate")
                .then()
                .statusCode(200);

        var entries = auditTrailStore.findByIdempotencyKey("idem-audit-5002");
        assertThat(entries).hasSize(2);
        assertThat(entries.stream().filter(entry -> entry.replayed()).count()).isEqualTo(1);
        assertThat(entries.stream().filter(entry -> !entry.replayed()).count()).isEqualTo(1);
    }
}
