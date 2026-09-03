# Strasbourg — C4 System Context

## Status

Defined for current showcase scope (loan origination + merchant identity verification).

## Scope

This document defines the **System Context (C4 Level 1)** for Strasbourg as it exists today:

- Merchant owner submits a loan application.
- Strasbourg verifies merchant identity via a SAP S/4 Anti-Corruption Layer (ACL).
- Strasbourg returns a loan origination decision.
- Strasbourg persists lifecycle, audit trail, and outbox events.

---

## C4 Level 1 — System Context

```mermaid
flowchart LR
    owner["Merchant Owner<br/>(Applicant)"]
    ops["Operations / Risk Analyst"]
    strasbourg["Strasbourg<br/>Lending Origination Platform"]
    sap["SAP S/4<br/>Merchant Data & Verification"]
    kafka["Kafka / Event Backbone<br/>(mocked in current runtime)"]
    idp["OIDC Identity Provider<br/>(profile-ready, disabled locally)"]

    owner -->|Submit loan application<br/>POST /api/loan-applications| strasbourg
    owner -->|Check application journey<br/>GET /api/loan-applications/{id}| strasbourg
    ops -->|Investigate decisions and journey| strasbourg

    strasbourg -->|Merchant identity verification via ACL| sap
    strasbourg -->|Publish domain/lifecycle events via outbox| kafka
    strasbourg -->|Token validation (prod profile)| idp
```

---

## People and External Systems

### People

1. **Merchant Owner (Applicant)**  
   Submits loan applications for their merchant and checks application journey.
2. **Operations / Risk Analyst**  
   Reviews manual-review outcomes and traces decisions with audit/journey data.

### Software Systems

1. **Strasbourg (System of Interest)**  
   Loan origination application built with Quarkus, using DDD + Hexagonal Architecture.
2. **SAP S/4**  
   Source system for merchant identity verification, accessed only through an ACL adapter.
3. **Kafka/Event Backbone**  
   Target for outbox-published domain events (currently mocked in local showcase).
4. **OIDC Identity Provider**  
   Production-profile authentication/authorization dependency (disabled in local default profile).

---

## Relationship Summary

| Source | Target | Relationship | Notes |
|---|---|---|---|
| Merchant Owner | Strasbourg | Submit/read loan application journey APIs | Main user-facing flow |
| Operations/Risk Analyst | Strasbourg | Investigate decision outcomes | Via journey + audit data |
| Strasbourg | SAP S/4 | Validate merchant identity through ACL | SAP model isolated from domain |
| Strasbourg | Kafka/Event Backbone | Publish outbox events | Mock publisher in current runtime |
| Strasbourg | OIDC Identity Provider | Validate access tokens | Active in production profile |

---

## Boundary and Responsibility

**Inside Strasbourg boundary**

- Loan application submission and retrieval.
- Applicant (owner) verification (currently mocked).
- Merchant verification orchestration and decisioning.
- Persistence of lifecycle (`SUBMITTED`, `VERIFIED`, `DECIDED`).
- Audit trail and transactional outbox handling.

**Outside Strasbourg boundary**

- Authoritative merchant data in SAP S/4.
- Enterprise messaging infrastructure (Kafka).
- Enterprise identity provider (OIDC).

---

## Out of Scope (for current showcase)

- Real credit bureau integration.
- Real fraud/affordability engines.
- Loan servicing/disbursement systems.
- Full manual-review operations workflow UI.

