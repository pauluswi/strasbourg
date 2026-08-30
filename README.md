# Strasbourg

> **Cloud-Native Lending Origination & Decisioning Platform**
>
> A production-inspired Financial Services platform built with **Java 25** and **Quarkus**, demonstrating modern lending architecture, automated decisioning, event-driven workflows, reliability patterns, and cloud-native deployment.

[![Java](https://img.shields.io/badge/Java-25-orange)]()
[![Quarkus](https://img.shields.io/badge/Quarkus-3.x-blue)]()
[![Architecture](https://img.shields.io/badge/Architecture-Modular%20Monolith-success)]()
[![Domain](https://img.shields.io/badge/Domain-Financial%20Services-blueviolet)]()
[![License](https://img.shields.io/badge/License-MIT-green)]()

---

# 🏦 What is Strasbourg?

**Strasbourg** is a cloud-native **Lending Origination and Decisioning Platform** designed to manage the lifecycle of a loan application.

The platform processes a loan application from submission through:

* Eligibility assessment
* Credit assessment
* Fraud assessment
* Affordability analysis
* Automated decisioning
* Manual review
* Loan offer generation
* Customer acceptance
* Downstream loan management handover

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
                         Loan Applicant
                                │
                                ▼
                      Submit Loan Application
                                │
                                ▼
                         Validation
                                │
                                ▼
                     Eligibility Assessment
                                │
                                ▼
                    Automated Assessments
                                │
           ┌────────────────────┼────────────────────┐
           │                    │                    │
           ▼                    ▼                    ▼

     Credit Assessment    Fraud Assessment    Affordability

           │                    │                    │
           └────────────────────┼────────────────────┘
                                │
                                ▼
                         Decision Engine
                                │
              ┌─────────────────┼──────────────────┐
              │                 │                  │
              ▼                 ▼                  ▼

          APPROVED          DECLINED        MANUAL_REVIEW
              │
              ▼
          Loan Offer
              │
              ▼
          Customer Acceptance
              │
              ▼
          Loan Management Handover
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
│  │  Credit │ Fraud │ Affordability │ Identity        │   │
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

---

# ⚙️ Technology Stack

| Area           | Technology                   |
| -------------- | ---------------------------- |
| Language       | **Java 25**                  |
| Framework      | **Quarkus**                  |
| Build Tool     | Maven                        |
| Database       | PostgreSQL                   |
| Cache          | Redis                        |
| Messaging      | Apache Kafka                 |
| Authentication | Keycloak / OpenID Connect    |
| Architecture   | DDD + Hexagonal Architecture |
| Deployment     | Docker + Kubernetes          |
| Cloud Target   | AWS                          |

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
DECLINED
MANUAL_REVIEW
PENDING
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

Strasbourg prevents duplicate loan applications.

```http
POST /loan-applications

Idempotency-Key: abc-123
```

Repeated requests using the same idempotency key must not create multiple loan applications.

---

# 📦 Project Structure

```text
strasbourg/
│
├── README.md
├── ARC42.md
│
├── docs/
│   ├── adr/
│   ├── c4/
│   └── diagrams/
│
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── strasbourg/
│   │
│   └── test/
│
├── docker/
├── kubernetes/
│
└── docker-compose.yml
```

---

# 🚀 Getting Started

> ⚠️ Strasbourg is currently under active development.

## Prerequisites

```text
Java 25
Maven
Docker
Docker Compose
```

The development environment will include:

* PostgreSQL
* Apache Kafka
* Redis
* Keycloak

## Clone the Repository

```bash
git clone https://github.com/pauluswi/strasbourg.git

cd strasbourg
```

## Run Infrastructure

```bash
docker compose up -d
```

## Run the Application

```bash
./mvnw quarkus:dev
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
ARC42.md
```

Strasbourg also documents architecture using:

* **arc42**
* **C4 Model**
* **Architecture Decision Records (ADRs)**

---

# 🗺️ Roadmap

## Phase 1 — Architecture Foundation

* [x] Define project scope
* [x] Create architecture documentation
* [ ] Define C4 System Context
* [ ] Define C4 Container Architecture
* [ ] Create Architecture Decision Records

## Phase 2 — Core Lending Domain

* [ ] Loan Application
* [ ] Application validation
* [ ] Eligibility assessment
* [ ] Domain model
* [ ] State transitions

## Phase 3 — Automated Assessments

* [ ] Credit assessment
* [ ] Fraud assessment
* [ ] Affordability assessment
* [ ] External provider simulators

## Phase 4 — Decisioning

* [ ] Rules-based decision engine
* [ ] Explainable decisions
* [ ] Manual review workflow

## Phase 5 — Event-Driven Architecture

* [ ] Apache Kafka integration
* [ ] Domain events
* [ ] Transactional Outbox
* [ ] Idempotent consumers

## Phase 6 — Production Readiness

* [ ] Authentication and authorization
* [ ] Observability
* [ ] Resilience patterns
* [ ] Integration tests
* [ ] Architecture tests

## Phase 7 — Cloud Deployment

* [ ] Docker
* [ ] Kubernetes
* [ ] Helm
* [ ] AWS deployment
* [ ] CI/CD pipeline

---

# 🎓 What This Project Demonstrates

### Software Engineering

* Java 25
* Quarkus
* REST APIs
* Kafka
* PostgreSQL
* Redis

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
