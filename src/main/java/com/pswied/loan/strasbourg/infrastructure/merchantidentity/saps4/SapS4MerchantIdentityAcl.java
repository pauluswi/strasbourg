package com.pswied.loan.strasbourg.infrastructure.merchantidentity.saps4;

import com.pswied.loan.strasbourg.application.merchantidentity.MerchantIdentityValidationPort;
import com.pswied.loan.strasbourg.domain.merchantidentity.MerchantIdentityStatus;
import com.pswied.loan.strasbourg.domain.merchantidentity.MerchantIdentityValidationRequest;
import com.pswied.loan.strasbourg.domain.merchantidentity.MerchantIdentityValidationResult;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.Locale;

@ApplicationScoped
public class SapS4MerchantIdentityAcl implements MerchantIdentityValidationPort {

    @Override
    public MerchantIdentityValidationResult validate(MerchantIdentityValidationRequest request) {
        SapS4ValidationRequest sapRequest = toSapRequest(request);
        SapS4ValidationResponse sapResponse = mockSapS4Lookup(sapRequest);
        return toValidationResult(request, sapResponse);
    }

    private SapS4ValidationRequest toSapRequest(MerchantIdentityValidationRequest request) {
        return new SapS4ValidationRequest(
                request.merchantId().trim().toUpperCase(Locale.ROOT),
                request.legalName().trim(),
                request.taxNumber().trim()
        );
    }

    private SapS4ValidationResponse mockSapS4Lookup(SapS4ValidationRequest request) {
        String joined = (request.merchantId() + " " + request.legalName() + " " + request.taxNumber())
                .toLowerCase(Locale.ROOT);

        if (joined.contains("reject") || joined.contains("blocked")) {
            return new SapS4ValidationResponse(
                    SapS4ValidationStatus.REJECTED,
                    "SAP S/4 rejected the merchant record",
                    "SAP-S4-REJ-" + request.merchantId()
            );
        }

        if (joined.contains("review") || request.taxNumber().endsWith("9")) {
            return new SapS4ValidationResponse(
                    SapS4ValidationStatus.MANUAL_REVIEW,
                    "SAP S/4 requires manual verification",
                    "SAP-S4-REV-" + request.merchantId()
            );
        }

        return new SapS4ValidationResponse(
                SapS4ValidationStatus.VERIFIED,
                "SAP S/4 confirmed the merchant identity",
                "SAP-S4-VER-" + request.merchantId()
        );
    }

    private MerchantIdentityValidationResult toValidationResult(
            MerchantIdentityValidationRequest request,
            SapS4ValidationResponse response
    ) {
        MerchantIdentityStatus status = switch (response.status()) {
            case VERIFIED -> MerchantIdentityStatus.VERIFIED;
            case REJECTED -> MerchantIdentityStatus.REJECTED;
            case MANUAL_REVIEW -> MerchantIdentityStatus.MANUAL_REVIEW;
        };

        return new MerchantIdentityValidationResult(
                request.merchantId().trim(),
                status,
                response.message(),
                "SAP_S4",
                response.referenceId(),
                Instant.now()
        );
    }

    private enum SapS4ValidationStatus {
        VERIFIED,
        REJECTED,
        MANUAL_REVIEW
    }

    private record SapS4ValidationRequest(String merchantId, String legalName, String taxNumber) {
    }

    private record SapS4ValidationResponse(
            SapS4ValidationStatus status,
            String message,
            String referenceId
    ) {
    }
}
