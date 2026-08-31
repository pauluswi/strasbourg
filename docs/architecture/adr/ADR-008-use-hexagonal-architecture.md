# ADR-008 — Use Hexagonal Architecture

## Status
Accepted

## Context
The domain must stay isolated from infrastructure and external systems.

## Decision
Structure modules using Hexagonal Architecture (Ports and Adapters).

## Alternatives
- Traditional layered architecture
- Direct infrastructure access from domain code

## Consequences
- Improves testability and separation of concerns
- Keeps business rules independent of adapters
- Requires explicit ports and adapter design
