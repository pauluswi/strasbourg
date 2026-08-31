# ADR-005 — Use Transactional Outbox Pattern

## Status
Accepted

## Context
Business data changes and event publication must remain consistent.

## Decision
Use the Transactional Outbox Pattern for reliable domain event publication.

## Alternatives
- Publish directly to Kafka inside application logic
- Rely on database change capture only

## Consequences
- Prevents lost events after successful database commits
- Adds an outbox publisher component
- Introduces eventual publication latency
