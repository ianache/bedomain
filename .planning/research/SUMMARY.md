# Project Research Summary

**Project:** bedomain - Business Entities Management Domain Service
**Domain:** Microservice for managing dynamic business entities with configurable state machines
**Researched:** 2026-03-01
**Confidence:** HIGH

## Executive Summary

This is a **Business Entities Management Microservice** — a domain service that allows other services to define and manage business entities with dynamic schemas and configurable state machines. Think of it as a "stateful entity store" where entity types, properties, and valid state transitions are defined at runtime rather than hardcoded.

The recommended approach is a **Spring Boot 3.5.x + Java 21** application using **layered architecture with DDD aggregates** for state machine management. Key infrastructure includes MySQL 8 (persistence), Redis (caching), Kafka (event streaming), and Keycloak (authentication). This is a well-established stack with extensive documentation and enterprise adoption.

The main risks are:
1. **State machine validation bypass** — transitions must be atomic with rule validation
2. **Dual-write problem** — DB commits must be followed by event publishing via outbox pattern
3. **Cache staleness** — Redis cache must invalidate on state transitions, not reads

These pitfalls are well-understood in the industry and have standard mitigation patterns. The research provides clear phase ordering based on feature dependencies.

## Key Findings

### Recommended Stack

**Core technologies:**
- **Spring Boot 3.5.x** — Application framework with Java 21 support, Jakarta EE 10, virtual threads
- **Java 21 (LTS)** — Required by project spec; full virtual threads, pattern matching, records
- **MySQL 8.0** — Primary database with JSON support, window functions, CTEs
- **Redis 7.x** — Caching layer with HA cluster support
- **Kafka 3.7.x** — Event streaming with KRaft mode
- **Keycloak 26.x** — OAuth2/OIDC identity provider
- **Spring Security 6.4.x** — JWT validation with built-in JWT decoder and JWKSet support

Supporting libraries: Flyway (migrations), MapStruct (DTO mapping), Lombok (boilerplate), Resilience4j (circuit breakers), Micrometer + OpenTelemetry (observability).

### Expected Features

**Must have (table stakes):**
- Entity Type CRUD API — Define entity types with dynamic properties
- Entity Instance CRUD — Create, read, update, delete entity instances
- Property Specifications — Schema-like definition per entity type
- State Machine Configuration — Define states and transitions
- State Transitions with Validation — Enforce valid transitions only
- State History Tracking — Full audit trail (who, when, from→to)
- REST API Authentication — JWT via Keycloak per architecture spec

**Should have (competitive):**
- Event-Driven Architecture (Kafka) — Publish business events for downstream services
- Redis Caching Layer — High-throughput performance
- Property-Level Validation — Declarative validation rules

**Defer (v2+):**
- GraphQL API — Not needed per PROJECT.md, adds complexity
- Multi-tenancy — Significant security complexity, single tenant for v1
- Complex Rules Engine — State machine transitions sufficient for v1

### Architecture Approach

**Layered architecture with DDD aggregates:**
- **Controller Layer** — HTTP handling, validation, response formatting
- **Service Layer** — Business logic orchestration, transaction management
- **Domain Layer** — Core entities with behavior, state machine logic, aggregate roots
- **Repository Layer** — Spring Data JPA abstractions
- **Event Publisher** — Domain events via Kafka
- **Cache Layer** — Spring Cache with Redis

Major components:
1. **EntityType** — Defines entity schema (properties, types, validation rules)
2. **BusinessEntity** — Aggregate root managing entity instance state and history
3. **StateMachine** — Configurable states and allowed transitions per entity type

### Critical Pitfalls

1. **State Machine Validation Bypass** — Transitions must validate against rules within same transaction. Use optimistic locking with version field. Avoid race conditions by loading rules atomically with state change.

2. **Dual-Write Problem** — DB writes succeed but Kafka events fail to publish. Implement Transactional Outbox Pattern: write events to outbox table in same DB transaction, use Kafka connector to poll and publish.

3. **Cached Entity State Stale** — Redis returns old state after transition completes. Invalidate cache as part of state transition write operation, not on read. Use write-through invalidation.

4. **Entity Type Configuration Drift** — Modifying entity type breaks existing instances. Version entity type configurations; entities reference specific version, not latest.

5. **JWT Validation Performance** — Remote Keycloak calls on every request cause latency spikes. Use local JWT validation with cached JWKS, reasonable cache TTL (hours).

## Implications for Roadmap

Based on research, suggested phase structure:

### Phase 1: Entity Type Management
**Rationale:** Foundation layer — all other features depend on having entity type definitions. Entity instances cannot exist without knowing their type schema.
**Delivers:** Entity Type CRUD API, Property Specifications, Entity Instance CRUD
**Avoids:** Pitfall #4 (Entity Type Drift) — implement versioning from the start

### Phase 2: State Machine Core + Authentication
**Rationale:** Core business logic — once entities exist, they need lifecycle management. Authentication is required before exposing state transition endpoints.
**Delivers:** State Machine Configuration, State Transitions with Validation, State History Tracking, JWT Authentication via Keycloak
**Avoids:** Pitfall #1 (State Machine Validation) — implement atomic validation from start
**Uses:** Keycloak integration, JWKS caching (Pitfall #5 mitigation)

### Phase 3: Event Publishing
**Rationale:** Integration layer — once state transitions work, publish events for downstream consumption. Must address dual-write problem before production.
**Delivers:** Kafka event publishing on state changes, Transactional Outbox Pattern
**Avoids:** Pitfall #2 (Dual-Write), Pitfall #6 (Event Payload Mismatch)

### Phase 4: Caching Layer
**Rationale:** Performance optimization — after core functionality works, add Redis caching. Must ensure cache invalidation works correctly.
**Delivers:** Redis caching with write-through invalidation
**Avoids:** Pitfall #3 (Stale Cache)

### Phase Ordering Rationale

- **Why Phase 1 first:** Entity type definitions are prerequisite for all other features (FEATURES.md dependency graph shows Entity Instance CRUD requires Entity Type Definition)
- **Why Phase 2 second:** State machine is core value proposition; authentication must be in place before exposing business logic
- **Why Phase 3 before 4:** Event publishing is a functional requirement; caching is performance optimization
- **Grouping rationale:** Each phase builds on previous — cannot test state transitions without entities, cannot test events without transitions
- **Pitfall mitigation:** Each phase explicitly addresses its related pitfalls (see phase descriptions)

### Research Flags

Phases likely needing deeper research during planning:
- **Phase 2 (State Machine):** Complex business logic, may need research on specific state machine edge cases
- **Phase 3 (Events):** Kafka integration patterns, outbox pattern implementation details

Phases with standard patterns (skip research-phase):
- **Phase 1 (Entity Types):** Standard CRUD patterns, well-documented
- **Phase 4 (Caching):** Standard Spring Cache patterns

## Confidence Assessment

| Area | Confidence | Notes |
|------|------------|-------|
| Stack | HIGH | Spring Boot 3.5 + Java 21 is well-documented, version compatibility verified |
| Features | MEDIUM | Based on competitor analysis and industry patterns; some features inferred |
| Architecture | HIGH | Standard layered + DDD patterns, detailed component breakdown |
| Pitfalls | MEDIUM | Well-documented industry patterns with standard mitigations |

**Overall confidence:** HIGH

### Gaps to Address

- **State machine implementation specifics:** Research which library (Spring State Machine vs custom) best fits requirements — needs decision during Phase 2 planning
- **Kafka topic design:** Number of partitions, retention policies — needs integration with downstream consumers
- **Keycloak realm configuration:** Detailed role-based access model — needs requirements clarification

## Sources

### Primary (HIGH confidence)
- Spring Boot 3.5.9 Release — https://spring.io/blog/2025/12/18/spring-boot-3-5-9-available-now
- Spring Security 6.4 Blog — https://spring.io/blog/2024/11/24/bootiful-34-security/
- Keycloak 26.x Release Notes — https://www.keycloak.org/docs/latest/release_notes/index.html

### Secondary (MEDIUM confidence)
- Spring State Machine documentation — https://docs.spring.io/spring-statemachine/docs/current/reference/
- Dual Write Problem — https://andrelucas.io/the-dual-write-problem-in-practice-spring-boot-kafka-and-postgresql-f77980e9ae0e
- Competitor analysis: Athennian, Newton, Adyen LEM APIs

### Tertiary (LOW confidence)
- Industry best practices for entity versioning — needs validation during implementation

---
*Research completed: 2026-03-01*
*Ready for roadmap: yes*
