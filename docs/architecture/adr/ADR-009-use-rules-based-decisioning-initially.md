# ADR-009 — Use Rules-Based Decisioning Initially

## Status
Accepted

## Context
The initial lending decisioning capability must be explainable and easy to reason about.

## Decision
Use deterministic rules-based decisioning for the first version of the decision engine.

## Alternatives
- Machine learning scoring
- External decision engine

## Consequences
- Decisions are explainable
- Easier to validate and test
- Later decision engines can be integrated without changing the core domain contract
