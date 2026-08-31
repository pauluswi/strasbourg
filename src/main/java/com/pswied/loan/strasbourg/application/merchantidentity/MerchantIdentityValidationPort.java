package com.pswied.loan.strasbourg.application.merchantidentity;

import com.pswied.loan.strasbourg.domain.merchantidentity.MerchantIdentityValidationRequest;
import com.pswied.loan.strasbourg.domain.merchantidentity.MerchantIdentityValidationResult;

public interface MerchantIdentityValidationPort {
    MerchantIdentityValidationResult validate(MerchantIdentityValidationRequest request);
}
