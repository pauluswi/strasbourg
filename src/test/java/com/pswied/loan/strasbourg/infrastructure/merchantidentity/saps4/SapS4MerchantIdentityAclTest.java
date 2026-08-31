package com.pswied.loan.strasbourg.infrastructure.merchantidentity.saps4;

import com.pswied.loan.strasbourg.domain.merchantidentity.MerchantIdentityStatus;
import com.pswied.loan.strasbourg.domain.merchantidentity.MerchantIdentityValidationRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SapS4MerchantIdentityAclTest {

    private final SapS4MerchantIdentityAcl acl = new SapS4MerchantIdentityAcl();

    @Test
    void returnsVerifiedForCleanMerchantData() {
        var result = acl.validate(new MerchantIdentityValidationRequest("m-1001", "Acme Trading", "TAX-1001"));

        assertThat(result.status()).isEqualTo(MerchantIdentityStatus.VERIFIED);
        assertThat(result.sourceSystem()).isEqualTo("SAP_S4");
        assertThat(result.externalReference()).startsWith("SAP-S4-VER-");
    }

    @Test
    void returnsManualReviewWhenSapNeedsHumanCheck() {
        var result = acl.validate(new MerchantIdentityValidationRequest("m-1002", "Acme Review Ltd", "TAX-1009"));

        assertThat(result.status()).isEqualTo(MerchantIdentityStatus.MANUAL_REVIEW);
        assertThat(result.reason()).contains("manual verification");
    }

    @Test
    void returnsRejectedWhenSapFindsAProblem() {
        var result = acl.validate(new MerchantIdentityValidationRequest("m-1003", "Blocked Merchant", "TAX-1003"));

        assertThat(result.status()).isEqualTo(MerchantIdentityStatus.REJECTED);
        assertThat(result.reason()).contains("rejected");
    }
}
