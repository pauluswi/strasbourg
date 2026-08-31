# ADR-006 — Use PostgreSQL as the System of Record

## Status
Accepted

## Context
The platform needs a reliable primary datastore for loan applications, decisions, and audit-friendly persistence.

## Decision
Use PostgreSQL as the system of record.

## Alternatives
- MySQL
- A NoSQL primary store
- Redis as primary persistence

## Consequences
- Strong transactional semantics
- Good fit for business records and relational data
- Redis remains a cache and idempotency aid, not the source of truth
