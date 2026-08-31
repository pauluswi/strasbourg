package com.pswied.loan.strasbourg.interfaces.rest.merchantidentity;

import com.pswied.loan.strasbourg.application.merchantidentity.MerchantIdentityValidationService;
import com.pswied.loan.strasbourg.domain.merchantidentity.MerchantIdentityValidationRequest;
import com.pswied.loan.strasbourg.domain.merchantidentity.MerchantIdentityValidationResult;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/merchant-identities")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MerchantIdentityResource {

    private final MerchantIdentityValidationService validationService;

    @Inject
    public MerchantIdentityResource(MerchantIdentityValidationService validationService) {
        this.validationService = validationService;
    }

    @POST
    @Path("/validate")
    public MerchantIdentityValidationResult validate(@Valid MerchantIdentityValidationRequest request) {
        return validationService.validate(request);
    }
}
