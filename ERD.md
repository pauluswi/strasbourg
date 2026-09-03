# ERD

This ERD reflects the persisted tables currently defined by JPA entities in the project.

```mermaid
erDiagram
    LOAN_APPLICATIONS {
        string loan_application_id PK
        string applicant_name
        string merchant_id
        decimal amount
        int tenor_months
        string status
        string lifecycle_stage
        string decision
        string decision_reason_code
        string eligibility_status
        text eligibility_reason
        string credit_assessment_status
        text credit_assessment_reason
        string fraud_assessment_status
        text fraud_assessment_reason
        string applicant_verification_status
        text applicant_verification_reason
        string merchant_verification_status
        text merchant_verification_reason
        string merchant_verification_source_system
        string merchant_verification_reference
        timestamp submitted_at
        timestamp verified_at
        timestamp decided_at
    }

    LOAN_APPLICATION_AUDIT_TRAIL {
        string audit_id PK
        string loan_application_id
        string lifecycle_stage
        string event_type
        text payload
        timestamp created_at
    }

    OUTBOX_EVENTS {
        string event_id PK
        string event_type
        string aggregate_id
        text payload
        string status
        int retry_count
        timestamp created_at
        timestamp published_at
        timestamp last_retried_at
        text last_error
    }

    MERCHANT_VALIDATION_IDEMPOTENCY {
        string idempotency_key PK
        string request_fingerprint
        string merchant_id
        string status
        text reason
        string source_system
        string external_reference
        timestamp validated_at
        timestamp created_at
    }

    MERCHANT_VALIDATION_AUDIT_TRAIL {
        string audit_id PK
        string idempotency_key
        string merchant_id
        text request_payload
        text mapped_sap_response
        string decision_status
        text decision_reason
        boolean replayed
        timestamp created_at
    }

    LOAN_APPLICATIONS ||--o{ LOAN_APPLICATION_AUDIT_TRAIL : "loan_application_id"
    MERCHANT_VALIDATION_IDEMPOTENCY ||--o{ MERCHANT_VALIDATION_AUDIT_TRAIL : "idempotency_key"
    LOAN_APPLICATIONS o{--o{ OUTBOX_EVENTS : "aggregate_id (LoanApplication)"
    MERCHANT_VALIDATION_IDEMPOTENCY o{--o{ OUTBOX_EVENTS : "aggregate_id (MerchantIdentity)"
```

## Notes

- Physical foreign keys are not declared in the entity mappings; relationships above are logical/application-level links.
- `outbox_events.aggregate_id` is polymorphic and stores IDs from different aggregates (for example `loan_application_id` and `merchant_id`).
