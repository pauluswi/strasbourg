# Strasbourg — C4 Container Architecture

## Status

Defined for the current implemented showcase (C4 Level 2).

## Scope

This document defines the **Container Architecture (C4 Level 2)** for Strasbourg, focusing on:

- loan application submission/read journey,
- merchant identity verification via SAP S/4 ACL,
- decisioning, lifecycle persistence, audit trail, and outbox publishing.

---

## C4 Level 2 — Container Diagram

```mermaid
flowchart LR
    owner["Merchant Owner"]
    analyst["Operations / Risk Analyst"]

    subgraph strasbourg["Strasbourg System Boundary"]
        api["Quarkus API Container<br/>REST interfaces + application services<br/>Java 25"]
        db["Relational Database Container<br/>H2 local / PostgreSQL prod"]
        publisher["Outbox Publisher Container<br/>Scheduled polling + retry/dead-letter"]
    end

    sap["SAP S/4 System<br/>(via ACL adapter)"]
    kafka["Kafka/Event Backbone<br/>(mocked in local runtime)"]
    idp["OIDC Provider<br/>(prod profile)"]

    owner -->|POST /api/loan-applications| api
    owner -->|GET /api/loan-applications/{id}| api
    analyst -->|Inspect application journey| api

    api -->|Read/write loan applications,<br/>audit trail, idempotency, outbox| db
    api -->|Merchant identity validation| sap
    api -->|AuthN/AuthZ token validation (prod)| idp

    publisher -->|Poll pending outbox events| db
    publisher -->|Mark published/retry/dead-letter| db
    publisher -->|Publish domain events| kafka
```

---

## Containers

| Container | Technology | Responsibilities |
|---|---|---|
| Quarkus API Container | Java 25, Quarkus | Exposes REST APIs, orchestrates applicant+merchant checks, computes decision, persists lifecycle state, writes audit and outbox entries, serves journey read model. |
| Relational Database Container | H2 (local), PostgreSQL (prod profile) | Stores loan applications, merchant validation idempotency records, merchant and loan audit trails, and outbox events. |
| Outbox Publisher Container | Quarkus Scheduler + application service | Polls pending outbox events, applies retry/backoff policy, moves exhausted events to dead-letter, publishes events to messaging boundary. |

---

## External Dependencies (from container view)

| External System | Integration | Purpose |
|---|---|---|
| SAP S/4 | ACL adapter (inside API container) | Merchant identity verification without leaking SAP model into domain. |
| Kafka/Event Backbone | Outbox publisher | Asynchronous domain/lifecycle event distribution (mocked in local showcase). |
| OIDC Identity Provider | Quarkus OIDC integration | Access token validation in production profile. |

---

## Data Ownership per Container

- **API Container** owns business orchestration and domain decisions.
- **Database Container** owns durability of state transitions and traceability.
- **Outbox Publisher Container** owns reliable event dispatch semantics (publish, retry, dead-letter).

---

## Key Runtime Flows (Container-level)

1. **Loan submission flow**
   - Merchant owner calls `POST /api/loan-applications`.
   - API container verifies merchant via SAP ACL, decides, writes lifecycle + audit + outbox to DB.
2. **Journey read flow**
   - Merchant owner or analyst calls `GET /api/loan-applications/{id}`.
   - API container reads lifecycle state + audit timeline from DB and returns full journey.
3. **Event dispatch flow**
   - Outbox publisher polls DB for pending events.
   - Publishes to messaging boundary, then marks `PUBLISHED`, or retries/dead-letters on failure.

---

## Notes

- The current local runtime keeps external systems mocked where possible for fast iteration.
- Container split is logical; in local development, API and publisher run in the same Quarkus process.
