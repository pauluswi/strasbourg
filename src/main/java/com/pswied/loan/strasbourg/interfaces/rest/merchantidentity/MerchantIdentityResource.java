package com.pswied.loan.strasbourg.interfaces.rest.merchantidentity;

import com.pswied.loan.strasbourg.application.idempotency.IdempotencyConflictException;
import com.pswied.loan.strasbourg.application.idempotency.MerchantValidationIdempotencyStorePort;
import com.pswied.loan.strasbourg.application.merchantidentity.MerchantIdentityValidationService;
import com.pswied.loan.strasbourg.domain.idempotency.MerchantValidationIdempotencyEntry;
import com.pswied.loan.strasbourg.domain.merchantidentity.MerchantIdentityValidationRequest;
import com.pswied.loan.strasbourg.domain.merchantidentity.MerchantIdentityValidationResult;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Locale;
import java.util.Optional;

@Path("/merchant-identities")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MerchantIdentityResource {

    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    private final MerchantIdentityValidationService validationService;
    private final MerchantValidationIdempotencyStorePort idempotencyStore;

    @Inject
    public MerchantIdentityResource(
            MerchantIdentityValidationService validationService,
            MerchantValidationIdempotencyStorePort idempotencyStore
    ) {
        this.validationService = validationService;
        this.idempotencyStore = idempotencyStore;
    }

    @POST
    @Path("/validate")
    public MerchantIdentityValidationResult validate(
            @HeaderParam(IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
            @Valid MerchantIdentityValidationRequest request
    ) {
        String normalizedKey = normalizeIdempotencyKey(idempotencyKey);
        String requestFingerprint = fingerprint(request);

        Optional<MerchantValidationIdempotencyEntry> existingEntry = idempotencyStore.findByKey(normalizedKey);
        if (existingEntry.isPresent()) {
            MerchantValidationIdempotencyEntry entry = existingEntry.orElseThrow();
            if (!entry.requestFingerprint().equals(requestFingerprint)) {
                throw conflict();
            }
            return entry.result();
        }

        MerchantIdentityValidationResult result = validationService.validate(request);
        try {
            idempotencyStore.save(normalizedKey, requestFingerprint, result);
        } catch (IdempotencyConflictException exception) {
            throw conflict();
        }
        return result;
    }

    private String normalizeIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new WebApplicationException(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("Idempotency-Key header is required")
                            .build()
            );
        }
        return idempotencyKey.trim();
    }

    private String fingerprint(MerchantIdentityValidationRequest request) {
        return "%s|%s|%s".formatted(
                normalize(request.merchantId()),
                normalize(request.legalName()),
                normalize(request.taxNumber())
        );
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private WebApplicationException conflict() {
        return new WebApplicationException(
                Response.status(Response.Status.CONFLICT)
                        .entity("Idempotency key already used for a different request")
                        .build()
        );
    }
}
