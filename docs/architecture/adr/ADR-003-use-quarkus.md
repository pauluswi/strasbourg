# ADR-003 — Use Quarkus

## Status
Accepted

## Context
Strasbourg needs a cloud-native Java framework that supports fast startup, lean runtime behavior, and modern integration patterns.

## Decision
Use Quarkus as the application framework.

## Alternatives
- Spring Boot
- Jakarta EE on another runtime

## Consequences
- Good fit for containerized deployment
- Strong support for cloud-native patterns
- Requires Quarkus-specific extension and testing choices
