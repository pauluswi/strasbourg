package com.pswied.loan.strasbourg.domain.merchantidentity;

import jakarta.validation.constraints.NotBlank;

public record MerchantIdentityValidationRequest(
        @NotBlank String merchantId,
        @NotBlank String legalName,
        @NotBlank String taxNumber
) {
}
