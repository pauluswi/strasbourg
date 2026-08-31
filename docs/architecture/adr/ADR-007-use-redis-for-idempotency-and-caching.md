# ADR-007 — Use Redis for Idempotency and Caching

## Status
Accepted

## Context
The platform needs a fast shared store for request deduplication and non-authoritative cached data.

## Decision
Use Redis for idempotency handling and caching.

## Alternatives
- Store idempotency markers only in PostgreSQL
- Use an in-memory cache

## Consequences
- Fast idempotency checks
- Supports short-lived cached lookups
- Must not be treated as the source of truth
