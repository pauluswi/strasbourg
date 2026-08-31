# ADR-001 — Use a Modular Monolith Architecture

## Status
Accepted

## Context
Strasbourg needs clear business boundaries without the operational overhead of distributed services during early development.

## Decision
Implement Strasbourg as a modular monolith with strong internal module boundaries.

## Alternatives
- Microservices from day one
- A single layered monolith with weak module boundaries

## Consequences
- Simpler deployment and debugging
- Stronger transactional consistency
- Lower early operational complexity
- Future service extraction remains possible when justified
