# Strasbourg — Architecture Documentation

> **Cloud-Native Lending Origination & Decisioning Platform**
>
> Architecture documentation based on the **arc42 template**.

---

# 1. Introduction and Goals

## 1.1 Requirements Overview

### Business Context

**Strasbourg** is a cloud-native Lending Origination and Decisioning Platform designed to manage the lifecycle of a loan application from submission through automated assessment, credit decisioning, offer generation, acceptance, and handover to downstream loan management and disbursement systems.

The platform is designed as a production-inspired Financial Services Industry (FSI) showcase that demonstrates how modern banking applications can be designed using:

* Java 25
* Quarkus
* Domain-Driven Design (DDD)
* Hexagonal Architecture
* Event-Driven Architecture
* Apache Kafka
* PostgreSQL
* Redis
* Kubernetes
* AWS

        **Strasbourg** focuses on the **loan origination lifecycle**, rather than loan servicing or core banking functionality.

The initial business flow is:

```text
Loan Application
        │
        ▼
Application Validation
        │
        ▼
Eligibility Assessment
        │
        ▼
Automated Assessments
        │
        ├── Credit Assessment
        ├── Fraud Assessment
        └── Affordability Assessment
        │
        ▼
Credit Decision
        │
        ├───────────────┬────────────────┐
        │               │                │
        ▼               ▼                ▼
     APPROVED        DECLINED      MANUAL_REVIEW
        │
        ▼
Loan Offer Generation
        │
        ▼
Customer Acceptance
        │
        ▼
Loan Account / Downstream Handover
```

The platform simulates integration with typical external banking and financial systems, including:

* Credit bureaus
* Fraud detection systems
* Identity verification providers
* Income verification services
* Core banking or loan management systems

These external systems are represented through well-defined integration ports and adapters so that implementations can be replaced without changing the core lending domain.

---

## 1.2 Business Goals

The primary business goals of Strasbourg are:

### BG-01 — Automate Loan Origination

Provide a digital workflow that manages loan applications from submission through lending decision and offer generation.

---

### BG-02 — Support Automated Decisioning

Enable automated lending decisions based on multiple assessments, including:

* Eligibility
* Creditworthiness
* Fraud risk
* Affordability

The system must support the following decision outcomes:

```text
APPROVED
DECLINED
MANUAL_REVIEW
PENDING
```

---

### BG-03 — Provide Explainable Decisions

A lending decision must not be a black box.

The system must record:

* Which rules were evaluated
* Which assessment data was used
* Which rules passed or failed
* The reasons contributing to the final decision

Example:

```json
{
  "decision": "MANUAL_REVIEW",
  "reasons": [
    {
      "rule": "DEBT_TO_INCOME_RATIO",
      "result": "FAILED",
      "reason": "Debt-to-income ratio exceeds the configured threshold"
    },
    {
      "rule": "MINIMUM_CREDIT_SCORE",
      "result": "PASSED"
    }
  ]
}
```

---

### BG-04 — Support Human Intervention

Not every lending decision should be fully automated.

The platform must support routing applications to human review when:

* Assessment results are inconclusive
* External systems are unavailable
* Risk thresholds require manual review
* Business rules explicitly require human approval

---

### BG-05 — Integrate Reliably with External Systems

Financial systems depend on multiple external providers.

The architecture must handle:

* Timeouts
* Temporary failures
* Retries
* Duplicate requests
* Delayed responses
* Partial failures

A failure of an external provider must not cause a loan application to be lost.

---

### BG-06 — Demonstrate Modern Financial Services Architecture

Strasbourg serves as a portfolio showcase demonstrating architecture and engineering practices applicable to modern banking and financial services systems.

The project emphasizes:

* Domain modeling
* Architectural trade-offs
* Reliability
* Auditability
* Security
* Observability
* Cloud-native deployment

---

# 1.3 Quality Goals

The following quality goals are prioritized for Strasbourg.

| Priority | Quality Goal    | Description                                                    |
| -------- | --------------- | -------------------------------------------------------------- |
| 🥇       | Reliability     | Loan applications and decisions must not be lost               |
| 🥈       | Auditability    | Lending decisions must be traceable and explainable            |
| 🥉       | Maintainability | Business capabilities must evolve independently                |
| 4        | Security        | Sensitive customer and financial information must be protected |
| 5        | Resilience      | External system failures must be handled gracefully            |
| 6        | Observability   | Operators must be able to understand system behavior           |
| 7        | Scalability     | The architecture must support increased application volume     |
| 8        | Performance     | Typical lending decisions should complete efficiently          |

---

## Reliability

The platform must ensure that a submitted loan application is not silently lost.

Key mechanisms include:

* PostgreSQL transactions
* Idempotent API processing
* Transactional Outbox
* Durable event delivery
* Retry strategies
* Dead Letter Queues where applicable

---

## Auditability

Every significant lending decision must be traceable.

The system should retain an audit trail for:

```text
Loan Application
      │
      ├── Submitted Data
      │
      ├── Eligibility Result
      │
      ├── Credit Assessment
      │
      ├── Fraud Assessment
      │
      ├── Affordability Assessment
      │
      ├── Decision Rules Evaluated
      │
      └── Final Decision
```

---

## Maintainability

The architecture must support changing lending rules without affecting unrelated business capabilities.

The system therefore emphasizes:

* Clear domain boundaries
* Modular architecture
* Separation of domain and infrastructure concerns
* Explicit dependencies
* Architecture tests

---

## Security

The system handles sensitive financial and personal information.

Security mechanisms include:

* OpenID Connect
* OAuth 2.0
* JWT
* Role-Based Access Control
* Secure secret management
* Audit logging
* Encryption in transit

---

## Resilience

External systems may fail independently.

The platform must support:

```text
Timeout
   │
   ▼
Retry
   │
   ▼
Circuit Breaker
   │
   ▼
Fallback / Manual Review
```

---

## Observability

Production operators should be able to answer:

* What happened to this loan application?
* Why was it declined?
* Which external system failed?
* Where is the processing bottleneck?
* How long does each assessment take?

The platform will use:

* OpenTelemetry
* Metrics
* Structured logging
* Distributed tracing
* Health checks

---

# 1.4 Stakeholders

| Stakeholder         | Interest                                                |
| ------------------- | ------------------------------------------------------- |
| Customer            | Fast and transparent loan application process           |
| Credit Analyst      | Ability to review applications and understand decisions |
| Underwriter         | Access to assessment and decision information           |
| Operations Team     | Ability to monitor and troubleshoot workflows           |
| Software Developers | Maintainable and understandable codebase                |
| Software Architects | Clear architectural boundaries and quality attributes   |
| Security Team       | Secure handling of customer information                 |
| Platform Engineers  | Reliable cloud-native deployment                        |
| External Providers  | Stable integration contracts                            |

---

# 2. Architecture Constraints

## 2.1 Technical Constraints

The initial implementation uses the following technology constraints.

| Area                  | Technology     |
| --------------------- | -------------- |
| Programming Language  | Java 25        |
| Application Framework | Quarkus        |
| Build Tool            | Maven          |
| Database              | PostgreSQL     |
| Messaging             | Apache Kafka   |
| Cache / Coordination  | Redis          |
| Authentication        | OpenID Connect |
| Identity Provider     | Keycloak       |
| Containerization      | Docker         |
| Orchestration         | Kubernetes     |
| Cloud Target          | AWS            |

---

## 2.2 Architectural Constraints

### Modular Monolith First

Strasbourg will initially be implemented as a **modular monolith**.

The application contains clearly separated business modules while being deployed as a single application.

```text
┌──────────────────────────────────────────────┐
│                  Strasbourg                   │
│                                              │
│  ┌──────────────┐  ┌──────────────────────┐ │
│  │ Application  │  │ Eligibility          │ │
│  └──────────────┘  └──────────────────────┘ │
│                                              │
│  ┌──────────────┐  ┌──────────────────────┐ │
│  │ Assessment   │  │ Decisioning          │ │
│  └──────────────┘  └──────────────────────┘ │
│                                              │
│  ┌──────────────┐  ┌──────────────────────┐ │
│  │ Offer        │  │ Integration          │ │
│  └──────────────┘  └──────────────────────┘ │
│                                              │
└──────────────────────────────────────────────┘
```

This decision prioritizes:

* Simpler deployment
* Faster development
* Easier debugging
* Strong transactional consistency
* Clear domain boundaries without distributed-system overhead

Future extraction into independently deployable services remains possible.

---

### API-First Design

External capabilities must be exposed through documented APIs.

The platform will use:

* REST APIs
* JSON
* OpenAPI specifications

---

### Event-Driven Integration

Asynchronous business events will be used where they provide clear value.

Examples include:

```text
LoanApplicationSubmitted

EligibilityAssessmentCompleted

CreditAssessmentCompleted

FraudAssessmentCompleted

AffordabilityAssessmentCompleted

CreditDecisionMade

LoanOfferCreated

LoanAccepted
```

Apache Kafka provides durable asynchronous communication.

---

### Cloud-Native Deployment

The application must be containerized and deployable to Kubernetes.

The architecture should avoid dependencies on a specific local environment.

---

# 2.3 Organizational Constraints

Strasbourg is initially developed as a portfolio and showcase project.

Therefore:

* The architecture should remain understandable by a small development team
* Infrastructure complexity must be justified
* Every major technology should solve a real architectural problem
* Technologies should not be added only for résumé value

---

# 2.4 Regulatory and Domain Constraints

Although Strasbourg is a showcase and does not process real customer data, the architecture is inspired by Financial Services Industry requirements.

Important considerations include:

* Sensitive personal data
* Financial decision auditability
* Explainable decisioning
* Secure access
* Data retention considerations
* Operational traceability

The project deliberately models these concerns without claiming compliance with a specific banking regulation.

---

# 3. Context and Scope

## 3.1 Business Context

The following diagram describes the primary business interactions.

```text
                    ┌──────────────────┐
                    │ Loan Applicant   │
                    └────────┬─────────┘
                             │
                             ▼
                  ┌────────────────────────┐
                  │                       │
                  │       Strasbourg        │
                  │                       │
                  │ Lending Origination    │
                  │ & Decisioning Platform │
                  │                       │
                  └───────────┬────────────┘
                              │
       ┌──────────────────────┼──────────────────────┐
       │                      │                      │
       ▼                      ▼                      ▼

Credit Bureau           Fraud Provider        Identity Provider

       │
       │
       ▼

┌───────────────────┐
│ Loan Management / │
│ Core Banking      │
└───────────────────┘
```

---

## 3.2 External Actors

### Loan Applicant

The customer submits a loan application and receives the resulting decision or offer.

---

### Credit Analyst

A credit analyst reviews applications requiring manual intervention.

Capabilities include:

* Reviewing application information
* Reviewing assessment results
* Viewing decision explanations
* Approving or declining applications

---

### Operations Team

Operations personnel monitor application processing and investigate failures.

---

## 3.3 External Systems

### Credit Bureau

Provides simulated credit information such as:

* Credit score
* Credit history
* Existing obligations

---

### Fraud Detection Provider

Provides fraud assessment information.

Example signals include:

* Application risk
* Identity anomalies
* Velocity checks
* Suspicious activity indicators

---

### Identity Verification Provider

Verifies customer identity.

---

### Income Verification Provider

Provides or validates income information.

---

### Loan Management / Core Banking System

Receives approved and accepted loan information for downstream processing.

This integration is abstracted through a dedicated adapter.

---

### SAP S/4

Provides merchant master data used for merchant identity validation.

The lending domain does not call SAP S/4 directly. It communicates through an **Anti-Corruption Layer** that translates between Strasbourg's merchant identity model and SAP's canonical structures.

---

# 3.4 Technical Context

```text
                         HTTPS / JSON
                              │
                              ▼

                    ┌──────────────────┐
                    │                  │
                    │    Strasbourg     │
                    │   Java 25        │
                    │   Quarkus        │
                    │                  │
                    └─────────┬────────┘
                              │

        ┌─────────────────────┼─────────────────────┐
        │                     │                     │

        ▼                     ▼                     ▼

 PostgreSQL                Kafka                  Redis

        │                     │
        │                     │
        ▼                     ▼

Persistent Data       Domain Events


Strasbourg
    │
    ├──── REST ──── Credit Bureau
    │
    ├──── REST ──── Fraud Provider
    │
    ├──── REST ──── Identity Provider
    │
    ├──── REST ──── Merchant Identity ACL ─── SAP S/4
    │
    └──── REST ──── Loan Management System
```

---

# 4. Solution Strategy

## 4.1 Architectural Approach

Strasbourg combines the following architectural approaches:

```text
Domain-Driven Design
        +
Hexagonal Architecture
        +
Modular Monolith
        +
Event-Driven Architecture
        +
Cloud-Native Deployment
```

Each approach addresses a different architectural concern.

---

## 4.2 Domain-Driven Design

The lending domain is divided into business capabilities.

Initial bounded contexts include:

```text
Strasbourg
│
├── Loan Application
│
├── Eligibility
│
├── Assessment
│   ├── Credit
│   ├── Fraud
│   └── Affordability
│
├── Decisioning
│
├── Offer Management
│
└── Integration
    ├── Merchant Identity
    └── Anti-Corruption Layer
```

These boundaries reduce unnecessary coupling between business capabilities.

---

## 4.3 Hexagonal Architecture

Each module follows a Ports and Adapters approach.

```text
                  ┌───────────────────┐
                  │                   │
                  │  REST / Messaging │
                  │      Adapters     │
                  │                   │
                  └─────────┬─────────┘
                            │
                            ▼
                  ┌───────────────────┐
                  │ Application Layer │
                  │                   │
                  │     Use Cases     │
                  └─────────┬─────────┘
                            │
                            ▼
                  ┌───────────────────┐
                  │   Domain Layer    │
                  │                   │
                  │ Business Rules    │
                  └─────────┬─────────┘
                            │
                            ▼
                  ┌───────────────────┐
                  │ Infrastructure    │
                  │                   │
                  │ DB / Kafka / APIs │
                  └───────────────────┘
```

The domain layer must not depend directly on:

* REST frameworks
* Kafka clients
* Database implementations
* External provider implementations

External systems such as SAP S/4 are accessed only through adapters and anti-corruption layers that translate their models into the lending domain's language.

---

## 4.4 Modular Monolith

The first implementation is deployed as a single application.

This decision provides:

### Advantages

* Simpler deployment
* Easier debugging
* Lower operational overhead
* Easier transactions
* Faster development

### Trade-off

Independent scaling and deployment of modules are limited.

However, clear module boundaries allow future extraction when justified.

---

## 4.5 Event-Driven Architecture

Kafka is used for asynchronous communication where appropriate.

Example:

```text
Loan Application Submitted
            │
            ▼
          Kafka
            │
     ┌──────┼───────────┐
     │      │           │
     ▼      ▼           ▼

Eligibility Credit     Fraud
Assessment  Assessment Assessment

     │      │           │
     └──────┼───────────┘
            │
            ▼

      Decision Engine
```

Events are not used simply because Kafka is available.

Synchronous processing remains appropriate when:

* Immediate responses are required
* Strong consistency is necessary
* The operation is simple

---

## 4.6 Reliable Event Publishing

Strasbourg uses the **Transactional Outbox Pattern** for reliable publication of domain events.

```text
                   PostgreSQL Transaction

        ┌───────────────────────────────────┐
        │                                   │
        │  Loan Application                 │
        │                                   │
        ├───────────────────────────────────┤
        │                                   │
        │  Outbox Event                     │
        │                                   │
        └──────────────────┬────────────────┘
                           │
                           ▼

                    Outbox Publisher
                           │
                           ▼

                         Kafka
```

This prevents inconsistencies where:

```text
Database Updated

BUT

Kafka Event Not Published
```

---

## 4.7 Decision Strategy

The initial decision engine uses deterministic, rules-based decisioning.

Example:

```text
IF creditScore < minimumScore
    DECLINE

IF debtToIncome > threshold
    MANUAL_REVIEW

IF fraudRisk > threshold
    DECLINE

IF all required assessments pass
    APPROVE
```

Each rule produces an explainable result.

The architecture allows future integration with:

* Business Rules Management Systems
* Machine Learning scoring
* External decision engines

without coupling these implementations directly to the core lending domain.

---

## 4.8 Resilience Strategy

External integrations are treated as unreliable.

The platform uses:

```text
Timeout
   │
   ▼
Retry
   │
   ▼
Circuit Breaker
   │
   ▼
Fallback
   │
   ▼
Manual Review
```

The fallback strategy should avoid automatically rejecting a customer simply because an external provider is temporarily unavailable.

---

## 4.9 Observability Strategy

Observability is implemented as a first-class architectural concern.

The platform supports:

```text
Structured Logs

Metrics

Distributed Tracing

Health Checks
```

The target observability stack is:

```text
Application
     │
     ├──── Metrics ─────► Prometheus
     │                       │
     │                       ▼
     │                    Grafana
     │
     └──── Traces ──────► OpenTelemetry
                             │
                             ▼
                        Trace Backend
```

---

# 5. Building Block View

> **Initial structure — to be expanded as implementation progresses.**

## Level 1 — Strasbourg

```text
┌─────────────────────────────────────────────────┐
│                                                 │
│                  Strasbourg                     │
│                                                 │
│    Lending Origination & Decisioning Platform   │
│                                                 │
└─────────────────────────────────────────────────┘
```

---

## Level 2 — Business Modules

```text
┌───────────────────────────────────────────────────────────┐
│                         Strasbourg                        │
│                                                           │
│  ┌────────────────┐      ┌─────────────────────────────┐ │
│  │ Loan           │      │ Eligibility                 │ │
│  │ Application    │      │                             │ │
│  └────────────────┘      └─────────────────────────────┘ │
│                                                           │
│  ┌─────────────────────────────────────────────────────┐ │
│  │ Assessment                                          │ │
│  │                                                     │ │
│  │ Credit │ Fraud │ Affordability │ Identity           │ │
│  └─────────────────────────────────────────────────────┘ │
│                                                           │
│  ┌────────────────┐      ┌─────────────────────────────┐ │
│  │ Decisioning    │      │ Offer Management            │ │
│  └────────────────┘      └─────────────────────────────┘ │
│                                                           │
│  ┌─────────────────────────────────────────────────────┐ │
│  │ Integration                                         │ │
│  │  Merchant Identity ACL                              │ │
│  └─────────────────────────────────────────────────────┘ │
│                                                           │
└───────────────────────────────────────────────────────────┘
```

Detailed module responsibilities will be expanded during implementation.

---

# 6. Runtime View

## 6.1 Loan Application Submission

```text
Customer
   │
   ▼
POST /loan-applications
   │
   ▼
Validate Request
   │
   ▼
Create Loan Application
   │
   ▼
Persist PostgreSQL
   │
   ▼
Create Outbox Event
   │
   ▼
LoanApplicationSubmitted
```

---

## 6.2 Automated Assessment

```text
LoanApplicationSubmitted
          │
          ▼
Assessment Coordinator
          │
    ┌─────┼─────────────┐
    │     │             │
    ▼     ▼             ▼

Credit  Fraud      Affordability
Check   Check      Check

    │     │             │
    └─────┼─────────────┘
          │
          ▼

Assessment Completed
```

---

## 6.3 Credit Decision

```text
Assessment Results
        │
        ▼
Decision Engine
        │
        ▼

Evaluate Rules

        │
        ▼

┌──────────────────────┐
│                      │
│ APPROVED             │
│ DECLINED             │
│ MANUAL_REVIEW        │
│                      │
└──────────────────────┘
```

---

## 6.4 Merchant Identity Validation

```text
Merchant Validation Required
          │
          ▼
Merchant Identity Use Case
          │
          ▼
Anti-Corruption Layer
          │
          ▼
SAP S/4 Merchant Validation
          │
          ▼
Validated / Rejected / Manual Review
```

---

# 7. Deployment View

## 7.1 Local Development

```text
Developer Machine
        │
        ▼

Docker Compose

        │
        ├──── PostgreSQL
        │
        ├──── Kafka
        │
        ├──── Redis
        │
        ├──── Keycloak
        │
        ├──── Prometheus
        │
        └──── Grafana
```

---

## 7.2 Target Cloud Architecture

```text
                         AWS
                          │
                          ▼

                   Kubernetes / EKS

                          │

              ┌───────────┴────────────┐
              │                        │

              ▼                        ▼

         Strasbourg                  Kafka

              │
              ├──────────────► RDS PostgreSQL
              │
              └──────────────► ElastiCache Redis
```

---

# 8. Crosscutting Concepts

## 8.1 Security

Security mechanisms include:

* OpenID Connect
* OAuth 2.0
* JWT
* RBAC
* TLS
* Secrets Management

Example roles:

```text
CUSTOMER

CREDIT_ANALYST

UNDERWRITER

OPERATIONS

ADMIN
```

---

## 8.2 Idempotency

Client requests must support idempotent processing.

Example:

```http
POST /loan-applications

Idempotency-Key: abc-123
```

Repeated requests using the same key must not create multiple loan applications.

Redis may be used to support idempotency handling.

---

## 8.3 Persistence

PostgreSQL is the primary system of record.

Redis is not considered the source of truth.

---

## 8.4 Messaging

Kafka provides asynchronous event communication.

Event consumers must assume:

```text
At-Least-Once Delivery
```

Therefore consumers must be idempotent.

---

## 8.5 Error Handling

Errors are categorized into:

```text
Business Errors

Validation Errors

Technical Errors

Integration Errors
```

Each category requires different handling.

---

# 9. Architecture Decisions

Major architectural decisions are documented separately as Architecture Decision Records under `docs/architecture/adr/`.

Initial ADRs include:

```text
ADR-001 — Use a Modular Monolith Architecture

ADR-002 — Use Java 25

ADR-003 — Use Quarkus

ADR-004 — Use Apache Kafka for Asynchronous Events

ADR-005 — Use Transactional Outbox Pattern

ADR-006 — Use PostgreSQL as the System of Record

ADR-007 — Use Redis for Idempotency and Caching

ADR-008 — Use Hexagonal Architecture

ADR-009 — Use Rules-Based Decisioning Initially
```

Each ADR documents:

* Context
* Decision
* Alternatives
* Consequences

---

# 10. Quality Requirements

## 10.1 Quality Tree

```text
Quality
│
├── Reliability
│   ├── Durable Persistence
│   ├── Event Delivery
│   └── Recovery
│
├── Security
│   ├── Authentication
│   ├── Authorization
│   └── Data Protection
│
├── Maintainability
│   ├── Modular Architecture
│   ├── Clear Dependencies
│   └── Automated Tests
│
├── Observability
│   ├── Logging
│   ├── Metrics
│   └── Tracing
│
└── Performance
    ├── Concurrent Assessment
    └── Efficient I/O
```

---

## 10.2 Key Quality Scenarios

### Scenario Q-01 — External Credit Bureau Timeout

**Given:** A loan application requires credit assessment.

**When:** The credit bureau does not respond within the configured timeout.

**Then:**

* The request times out
* Retry policy is applied
* Circuit breaker may open
* The application is not lost
* The application may be routed to `MANUAL_REVIEW`

---

### Scenario Q-02 — Duplicate Loan Submission

**Given:** A client submits a loan application.

**When:** The client retries due to a network failure.

**Then:**

* The same idempotency key is detected
* A duplicate application is not created
* The original result is returned

---

### Scenario Q-03 — Kafka Temporarily Unavailable

**Given:** A business event must be published.

**When:** Kafka is temporarily unavailable.

**Then:**

* The business transaction remains durable
* The event remains in the transactional outbox
* The event is published when Kafka becomes available

---

# 11. Risks and Technical Debt

## R-01 — Increasing Domain Complexity

Lending rules can become increasingly complex.

**Mitigation:**

Maintain clear domain boundaries and isolate decision rules.

---

## R-02 — External Provider Dependencies

External providers may behave unpredictably.

**Mitigation:**

* Timeouts
* Retry
* Circuit breakers
* Fallback workflows

---

## R-03 — Premature Microservices

The system could become unnecessarily distributed.

**Mitigation:**

Start with a modular monolith.

Extract services only when justified by:

* Independent scaling
* Team ownership
* Deployment independence
* Technology requirements

---

## R-04 — Overengineering

A portfolio project can accumulate unnecessary technologies.

**Mitigation:**

Every technology must address a documented architectural problem.

---

# 12. Glossary

| Term                   | Definition                                                               |
| ---------------------- | ------------------------------------------------------------------------ |
| Loan Origination       | The process of creating and evaluating a loan application                |
| Applicant              | A person or organization applying for a loan                             |
| Eligibility            | Initial determination of whether an applicant meets product requirements |
| Credit Assessment      | Evaluation of creditworthiness                                           |
| Affordability          | Assessment of ability to repay a loan                                    |
| Fraud Assessment       | Evaluation of potential fraudulent activity                              |
| Decisioning            | The process of determining the lending outcome                           |
| Manual Review          | Human evaluation of an application                                       |
| Loan Offer             | Proposed lending terms presented to an applicant                         |
| Underwriting           | Detailed evaluation of lending risk                                      |
| Transactional Outbox   | Pattern for reliably publishing events after database transactions       |
| Idempotency            | Guarantee that repeated requests do not produce duplicate effects        |
| Bounded Context        | A clear boundary around a domain model                                   |
| Hexagonal Architecture | Architecture separating domain logic from external infrastructure        |

---

# Appendix A — Technology Stack

```text
Language
└── Java 25

Framework
└── Quarkus

Architecture
├── DDD
├── Hexagonal Architecture
├── Modular Monolith
└── Event-Driven Architecture

Data
├── PostgreSQL
├── Redis
└── Flyway

Messaging
├── Apache Kafka
└── Transactional Outbox

Security
├── Keycloak
├── OpenID Connect
├── OAuth 2.0
└── JWT

Resilience
├── Timeout
├── Retry
├── Circuit Breaker
├── Bulkhead
└── Fallback

Observability
├── OpenTelemetry
├── Prometheus
├── Grafana
└── Structured Logging

Testing
├── JUnit 5
├── Mockito
├── AssertJ
├── Testcontainers
└── ArchUnit

Platform
├── Docker
├── Docker Compose
├── Kubernetes
└── Helm

Cloud
└── AWS
    ├── EKS
    ├── RDS PostgreSQL
    ├── MSK
    ├── ElastiCache
    ├── Secrets Manager
    └── CloudWatch
```

---

> **Architecture Principle**
>
> Strasbourg prioritizes clear domain boundaries, reliability, explainability, and operational simplicity. Technologies are selected to solve concrete architectural problems rather than to maximize the number of technologies used.
