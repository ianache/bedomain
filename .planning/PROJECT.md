# bedomain - Business Entities Management Domain Service

## What This Is

A SpringBoot-based microservice for managing business entity definitions, rules, and state machines. Provides a comprehensive solution for defining, tracking, and managing business entities throughout their lifecycle with built-in state machine support, event-driven architecture, and JWT-based security via Keycloak.

## Core Value

Centralized management of business entity definitions with declarative state transition rules, full audit history, and event-driven integration patterns for reactive downstream processing.

## Requirements

### Validated

(None yet — ship to validate)

### Active

- [ ] Business Entity Type CRUD API
- [ ] Business Entity Specification Management
- [ ] Business Entity Property Specifications
- [ ] State Machine Management (create, configure states and transitions)
- [ ] Entity Instance CRUD with State Transitions
- [ ] State History Tracking
- [ ] Business Event Type Management
- [ ] Kafka Event Publishing (business events topic)
- [ ] JWT Authentication via Keycloak
- [ ] Redis Caching Layer
- [ ] MySQL Persistence

### Out of Scope

- [GraphQL API] — REST API sufficient for v1
- [Multi-tenancy] — Single tenant for v1
- [Complex validation rules engine] — State machine transitions only
- [Entity versioning] — State history provides audit trail
- [Web UI] — API-first, consume from other services

## Context

**Technical Environment:**
- SpringBoot 3.x with Java 21
- MySQL 4.7 for persistence
- Redis HA Cluster for caching
- Apache Kafka for event streaming
- RedHat Keycloak for authentication

**Architecture Pattern:**
- Domain-Driven Design (DDD)
- Event-Driven Architecture
- Stateless microservice with externalized state

**Integration Points:**
- Keycloak: JWT token validation
- Kafka: topic.businessevents (domain events), topic.errorslog (logging)
- Redis: Caching layer
- MySQL: Primary data store

## Constraints

- **Tech Stack**: SpringBoot 3.x + Java 21 — Per architecture specification
- **Authentication**: Keycloak JWT — Enterprise security requirement
- **Database**: MySQL 4.7 — Per architecture specification
- **Cache**: Redis — High-availability cluster required

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| REST over GraphQL | Simpler to implement, matches Keycloak JWT flow | — Pending |
| Kafka for events | Architecture specifies Kafka topics for business events and error logging | — Pending |
| Redis caching | Required for high-throughput scenarios per architecture | — Pending |

---
*Last updated: 2026-03-01 after initialization*
