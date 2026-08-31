# ADR-004 — Use Apache Kafka for Asynchronous Events

## Status
Accepted

## Context
The platform needs durable asynchronous integration for domain events and workflow propagation.

## Decision
Use Apache Kafka for asynchronous event communication.

## Alternatives
- Synchronous REST-only integration
- RabbitMQ or another message broker

## Consequences
- Durable event transport
- Better fit for event-driven workflows
- Consumers must handle at-least-once delivery and idempotency
