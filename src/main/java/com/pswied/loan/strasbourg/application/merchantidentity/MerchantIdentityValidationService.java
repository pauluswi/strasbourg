package com.pswied.loan.strasbourg.application.merchantidentity;

import com.pswied.loan.strasbourg.domain.merchantidentity.MerchantIdentityValidationRequest;
import com.pswied.loan.strasbourg.domain.merchantidentity.MerchantIdentityValidationResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MerchantIdentityValidationService {

    private final MerchantIdentityValidationPort validationPort;

    @Inject
    public MerchantIdentityValidationService(MerchantIdentityValidationPort validationPort) {
        this.validationPort = validationPort;
    }

    public MerchantIdentityValidationResult validate(MerchantIdentityValidationRequest request) {
        return validationPort.validate(request);
    }
}
