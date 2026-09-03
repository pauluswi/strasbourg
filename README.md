# Strasbourg

> **Lending Origination Showcase (Java 25 + Quarkus)**
>
> A running showcase of loan origination where a merchant owner applies for a loan, the system verifies merchant identity via a SAP S/4 ACL (mocked), returns a decision, and persists lifecycle + outbox + audit trails.

[![Java](https://img.shields.io/badge/Java-25-orange)]()
[![Quarkus](https://img.shields.io/badge/Quarkus-3.x-blue)]()
[![Architecture](https://img.shields.io/badge/Architecture-Modular%20Monolith-success)]()
[![Domain](https://img.shields.io/badge/Domain-Financial%20Services-blueviolet)]()
[![License](https://img.shields.io/badge/License-MIT-green)]()

---

# 🏦 What is Strasbourg?

**Strasbourg** is a cloud-native **Lending Origination and Decisioning Platform** designed to manage the lifecycle of a loan application.

Current implemented flow:

* Merchant owner submits a loan application
* Automated assessments are executed (credit, fraud)
* Eligibility policy gate is executed before decisioning
* Applicant (owner) verification is run via mock adapter
* Merchant identity is verified through SAP S/4 ACL (mocked adapter)
* Origination decision is returned (`APPROVED`, `REJECTED`, `MANUAL_REVIEW`)
* Lifecycle is persisted (`SUBMITTED`, `VERIFIED`, `DECIDED`)
* Outbox + audit entries are written for traceability

Strasbourg is designed as a **production-inspired architecture showcase** for modern banking and Financial Services Industry (FSI) applications.

It focuses on solving realistic engineering problems such as:

* How do we prevent duplicate loan applications?
* How do we handle unreliable external credit providers?
* How do we explain an automated lending decision?
* How do we reliably publish business events?
* How do we recover when downstream systems fail?
* How do we maintain clear boundaries in a growing banking application?

---

# 🎯 Project Goals

Strasbourg is designed to demonstrate:

### 🏦 Financial Services Architecture

Modern architecture patterns applicable to:

* Banks
* Digital banks
* Fintech companies
* Lending platforms
* Embedded finance platforms

### ☕ Modern Java

Practical usage of:

* **Java 25**
* Virtual Threads where appropriate
* Records
* Sealed classes
* Pattern matching

### ⚡ Cloud-Native Java

Using:

* **Quarkus**
* Containerized deployment
* Kubernetes
* Cloud-native observability

### 🏗️ Software Architecture

Including:

* Domain-Driven Design
* Hexagonal Architecture
* Modular Monolith
* Event-Driven Architecture
* Transactional Outbox
* Architecture Decision Records

### 🛡️ Production Concerns

Including:

* Reliability
* Idempotency
* Resilience
* Security
* Auditability
* Observability

---

# 🔄 Lending Origination Flow

```text
                   Merchant Owner (Applicant)
                               │
                               ▼
                 POST /api/loan-applications
                               │
                               ▼
                 Eligibility + Credit + Fraud Checks
                               │
                               ▼
                 Mock Applicant Verification
                              │
                              ▼
                 Merchant Identity Validation
                               │
                               ▼
                    SAP S/4 ACL (Mock Adapter)
                               │
                               ▼
                       Origination Decision
                  (APPROVED / REJECTED / MANUAL_REVIEW)
                               │
                               ▼
         Persist Loan Lifecycle + Audit + Outbox Events
                               │
                               ▼
              GET /api/loan-applications/{id} Journey
```

---

# 🏗️ Architecture Overview

Strasbourg follows an **architect-first approach**.

The system starts as a:

# **Modular Monolith**

rather than immediately introducing multiple microservices.

```text
┌──────────────────────────────────────────────────────────┐
│                       Strasbourg                         │
│                                                          │
│  ┌─────────────────┐    ┌────────────────────────────┐   │
│  │ Loan            │    │ Eligibility                │   │
│  │ Application     │    │                            │   │
│  └─────────────────┘    └────────────────────────────┘   │
│                                                          │
│  ┌───────────────────────────────────────────────────┐   │
│  │ Assessment                                        │   │
│  │                                                   │   │
│  │  Credit │ Fraud │ Identity                      │   │
│  └───────────────────────────────────────────────────┘   │
│                                                          │
│  ┌─────────────────┐    ┌────────────────────────────┐   │
│  │ Decisioning     │    │ Offer Management           │   │
│  └─────────────────┘    └────────────────────────────┘   │
│                                                          │
│  ┌───────────────────────────────────────────────────┐   │
│  │ Integration                                       │   │
│  └───────────────────────────────────────────────────┘   │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

This approach provides:

* Clear domain boundaries
* Lower operational complexity
* Easier debugging
* Strong transactional consistency
* Faster development

> **Microservices are an architectural decision, not a default.**

---

# 🧠 Architectural Principles

## 1. Domain First

The architecture is organized around lending business capabilities:

```text
Loan Application
Eligibility
Assessment
Decisioning
Offer Management
Integration
```

## 2. Modular Monolith First

Strasbourg is initially deployed as a single application while maintaining strict internal module boundaries.

## 3. Infrastructure Must Not Control the Domain

The core business domain does not depend directly on:

* REST frameworks
* Kafka
* PostgreSQL
* Redis
* External providers

The project follows **Hexagonal Architecture (Ports and Adapters)**.

```text
                    ┌────────────────────┐
                    │ External Adapters  │
                    │ REST / Kafka / API │
                    └─────────┬──────────┘
                              │
                              ▼
                    ┌────────────────────┐
                    │ Application Layer  │
                    │ Use Cases          │
                    └─────────┬──────────┘
                              │
                              ▼
                    ┌────────────────────┐
                    │ Domain Layer       │
                    │ Business Rules     │
                    └────────────────────┘
```

## 4. External Systems Must Be Isolated

External enterprise systems are integrated through **Anti-Corruption Layers (ACLs)**.

For merchant identity validation, Strasbourg uses an ACL around **SAP S/4** so the lending domain keeps its own model and language instead of inheriting SAP terminology or data structures.

The ACL is responsible for:

* Translating Strasbourg merchant identity requests into SAP-compatible calls
* Mapping SAP responses back into the platform's domain model
* Shielding the core domain from SAP-specific schema and process changes
* Keeping validation failures explicit so the merchant workflow can route to manual review when needed

---

# ✅ Implemented Showcase APIs

* `POST /api/merchant-identities/validate`  
  Merchant verification with `Idempotency-Key`, audit persistence, and outbox event.
* `POST /api/loan-applications`  
  Submit loan application, run eligibility + credit + fraud + applicant + merchant checks, return decision, persist lifecycle/audit/outbox.
* `GET /api/loan-applications/{loanApplicationId}`  
  Read full journey including SAP verification result and lifecycle timeline.
* `POST /api/outbox/publish`  
  Manual trigger for outbox publishing (with scheduler also enabled).

---

# ⚙️ Technology Stack

| Area           | Technology                   |
| -------------- | ---------------------------- |
| Language       | **Java 25**                  |
| Framework      | **Quarkus**                  |
| Build Tool     | Maven                        |
| Database       | H2 (local default), PostgreSQL (prod profile) |
| Cache          | Redis dependency (not active in current flow) |
| Messaging      | Mock Kafka publisher + outbox model |
| Authentication | OIDC profile prepared (disabled by default local/test) |
| Architecture   | DDD + Hexagonal Architecture |
| Testing        | Quarkus Test, AssertJ, ArchUnit |
| Deployment     | Local-first showcase runtime |

---

# 🔐 Explainable Decisioning

Lending decisions should not be black boxes.

Strasbourg records the reasons behind a decision.

```json
{
  "applicationId": "loan-12345",
  "decision": "MANUAL_REVIEW",
  "reasons": [
    {
      "rule": "MINIMUM_CREDIT_SCORE",
      "result": "PASSED"
    },
    {
      "rule": "DEBT_TO_INCOME_RATIO",
      "result": "FAILED",
      "reason": "Debt-to-income ratio exceeds the configured threshold"
    }
  ]
}
```

Possible outcomes:

```text
APPROVED
REJECTED
MANUAL_REVIEW
```

---

# 🔄 Reliable Event Processing

Strasbourg uses the **Transactional Outbox Pattern**.

```text
              PostgreSQL Transaction

     ┌────────────────────────────────┐
     │                                │
     │  Loan Application              │
     │                                │
     ├────────────────────────────────┤
     │                                │
     │  Outbox Event                  │
     │                                │
     └───────────────┬────────────────┘
                     │
                     ▼
               Outbox Publisher
                     │
                     ▼
                 Apache Kafka
```

This prevents situations where the database is updated but the corresponding business event is not published.

---

# 🛡️ Idempotency

Strasbourg currently enforces idempotency on merchant identity verification.

```http
POST /merchant-identities/validate

Idempotency-Key: abc-123
```

Repeated requests with the same key and same payload replay the same result; the same key with a different payload returns `409 Conflict`.

---

# 📦 Project Structure

```text
strasbourg/
│
├── README.md
├── pom.xml
│
├── docs/
│   └── architecture/
│       ├── arc42.md
│       └── adr/
│
├── src/
│   ├── main/
│   │   ├── java/com/pswied/loan/strasbourg/
│   │   │   ├── domain/
│   │   │   ├── application/
│   │   │   ├── infrastructure/
│   │   │   └── interfaces/
│   │   └── resources/application.properties
│   └── test/
│       └── java/com/pswied/loan/strasbourg/
│
└── mvnw
```

---

# 🚀 Getting Started

> ⚠️ Strasbourg is currently under active development.

## Prerequisites

```text
Java 25
```

## Clone the Repository

```bash
git clone https://github.com/pauluswi/strasbourg.git

cd strasbourg
```

## Run the Application

```bash
./mvnw quarkus:dev
```

## Quick API Walkthrough

```bash
# 1) Merchant identity validation (idempotent)
curl -X POST http://localhost:8080/api/merchant-identities/validate \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: demo-merchant-1' \
  -d '{"merchantId":"m-1001","legalName":"Acme","taxNumber":"TAX-1001"}'

# 2) Submit loan application
curl -X POST http://localhost:8080/api/loan-applications \
  -H 'Content-Type: application/json' \
  -d '{"applicantName":"Alice Owner","merchantId":"m-1001","merchantLegalName":"Acme","merchantTaxNumber":"TAX-1001","amount":15000.00,"tenorMonths":24}'

# 3) Read the journey
curl http://localhost:8080/api/loan-applications/{loanApplicationId}
```

---

# 🧪 Testing Strategy

```text
                    End-to-End Tests
                           ▲
                    Integration Tests
                    Testcontainers
                           ▲
                    Architecture Tests
                       ArchUnit
                           ▲
                       Unit Tests
```

Technologies:

* JUnit 5
* Mockito
* AssertJ
* Testcontainers
* Quarkus Test
* ArchUnit

---

# ☁️ Target Deployment

```text
                           AWS
                            │
                            ▼
                      Kubernetes / EKS
                            │
               ┌────────────┼────────────┐
               │            │            │
               ▼            ▼            ▼
           Strasbourg       MSK      Observability
               │
        ┌──────┴──────┐
        ▼             ▼
   RDS PostgreSQL   ElastiCache
```

---

# 📚 Architecture Documentation

The complete architecture documentation is available in:

```text
docs/architecture/arc42.md
```

Strasbourg also documents architecture using:

* **arc42**
* **C4 Model**
* **Architecture Decision Records (ADRs)**

Core architecture docs:

```text
docs/architecture/c4-system-context.md
docs/architecture/c4-container-architecture.md
docs/architecture/adr/
```

Deployment docs:

```text
docs/deployment/aws-eks.md
```

---

# 🗺️ Roadmap

## Phase 1 — Architecture Foundation

* [x] Define project scope
* [x] Create architecture documentation
* [x] Define C4 System Context
* [x] Define C4 Container Architecture
* [x] Create Architecture Decision Records

## Phase 2 — Core Lending Domain

* [x] Loan Application (submit + read journey)
* [x] Application validation (merchant owner + merchant data)
* [x] Eligibility assessment
* [x] Domain model
* [x] State transitions (`SUBMITTED` -> `VERIFIED` -> `DECIDED`)

## Phase 3 — Automated Assessments

* [x] Credit assessment
* [x] Fraud assessment
* [x] Scope limited to credit + fraud (affordability intentionally skipped)
* [x] External provider simulators

## Phase 4 — Decisioning

* [x] Initial decision engine (`APPROVED` / `REJECTED` / `MANUAL_REVIEW`)
* [x] Explainable decisions (decision reason code + reasons in response)
* [ ] Manual review workflow

## Phase 5 — Event-Driven Architecture

* [ ] Apache Kafka real broker integration
* [x] Domain events (loan lifecycle + merchant validation)
* [x] Transactional Outbox (persisted, scheduler + retries/dead-letter)
* [ ] Idempotent consumers

## Phase 6 — Production Readiness

* [ ] Authentication and authorization
* [ ] Observability
* [ ] Resilience patterns
* [x] Integration tests (Quarkus + persistence + API showcase)
* [x] Architecture tests (ArchUnit)

## Phase 7 — Cloud Deployment

* [x] Docker (baseline Dockerfile)
* [x] Kubernetes (baseline manifests)
* [x] Helm (baseline chart)
* [x] AWS deployment template (EKS deployment guide + guarded workflow)
* [x] CI/CD pipeline (CI active, CD manual-only by default)

---

# 🎓 What This Project Demonstrates

### Software Engineering

* Java 25
* Quarkus
* REST APIs
* Outbox + mock Kafka publisher
* H2 (local) and PostgreSQL profile
* Redis-ready dependencies

### Software Architecture

* Domain-Driven Design
* Hexagonal Architecture
* Event-Driven Architecture
* Modular Monolith
* C4 Model
* arc42
* Architecture Decision Records

### Financial Services

* Lending Origination
* Credit Assessment
* Fraud Assessment
* Decisioning
* Explainability
* Auditability

### Cloud & Platform Engineering

* Docker
* Kubernetes
* AWS
* Observability
* CI/CD

---

# 👤 Author

**Slamet Widodo**

Software Architect | Java | Banking & Financial Services | Cloud Architecture

---

# ⭐ Architecture Philosophy

> **Start simple. Design for change. Add complexity only when the problem justifies it.**

Strasbourg intentionally starts as a **modular monolith** with clear domain boundaries.

The goal is not to demonstrate the maximum number of technologies.

The goal is to demonstrate **good architectural judgment**.

---

## 🏦 Strasbourg at a Glance

```text
                 FINANCIAL SERVICES
                         │
                         ▼
               LENDING ORIGINATION
                         │
                         ▼
                  JAVA 25 + QUARKUS
                         │
                         ▼
              DOMAIN-DRIVEN DESIGN
                         │
                         ▼
                 RELIABLE SYSTEMS
                         │
                         ▼
               CLOUD-NATIVE PLATFORM
```

**Strasbourg is a production-inspired showcase of modern lending architecture built around reliability, explainability, and maintainability.**
