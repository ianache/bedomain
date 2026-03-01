# Phase 1: Entity Foundation - Context

**Gathered:** 2026-03-01
**Status:** Ready for planning

<domain>
## Phase Boundary

This phase delivers the foundational entity management layer for the bedomain service. Users can define entity types with property specifications, create entity instances with attributes, and authenticate via Keycloak JWT. The scope includes REST API endpoints for CRUD operations, MySQL persistence, Redis caching, and health monitoring.

**Out of scope:** State machines, state transitions, history tracking, Kafka event publishing (future phases)

</domain>

<decisions>
## Implementation Decisions

### Architecture Pattern
- Domain-Driven Design (DDD) with clear separation: entities, repositories, services, controllers
- SpringBoot 3.x with Java 21
- Single-module Maven project (simplicity for v1)

### API Design
- RESTful API with JSON request/response
- Standard HTTP methods: GET (list/get), POST (create), PUT (update), DELETE (delete)
- Resource naming: `/api/v1/entity-types`, `/api/v1/entity-types/{id}/properties`, `/api/v1/entities`
- Pagination support for list endpoints
- Global exception handler with standardized error responses

### Data Persistence
- JPA/Hibernate for ORM
- MySQL 8.x database
- UUID primary keys for entities (generated server-side)
- Soft deletes where applicable (deleted flag vs physical delete)
- Audit fields: createdAt, createdBy, updatedAt, updatedBy on all entities

### Authentication & Authorization
- Keycloak JWT validation via Spring Security OAuth2 Resource Server
- All endpoints require valid JWT except `/actuator/health`
- User context extracted from JWT claims (sub, preferred_username)
- Role-based access controlled at method level

### Caching Strategy
- Redis for caching entity type lookups (by ID and by name)
- Cache-aside pattern: check cache first, load from DB on miss
- TTL-based expiration (configurable, default 1 hour)
- Cache invalidation on entity type updates/deletes

### Health & Monitoring
- Spring Boot Actuator for health endpoints
- `/actuator/health` (public), `/actuator/info` (public)
- Database connection pool metrics
- Redis connection status

</decisions>

<code_context>
## Existing Code Insights

### Reusable Assets
- None yet - greenfield project

### Established Patterns
- None yet - will establish in this phase

### Integration Points
- Keycloak: OAuth2 Resource Server configuration
- MySQL: DataSource configuration
- Redis: RedisTemplate for caching
- External: REST consumers

</code_context>

<specifics>
## Specific Ideas

**Technology Choices (from requirements):**
- SpringBoot 3.x + Java 21
- MySQL 4.7 (likely means MySQL 8.x compatible)
- Redis HA Cluster
- Keycloak for JWT
- Kafka deferred to Phase 3

**Data Types (from PROP-05):**
- STRING, NUMBER, DATE, BOOLEAN, TEXT

**Entity Instance Fields (from ENTY-06):**
- id (GUID)
- type reference
- attributes (JSON)
- creation date
- created by

</specifics>

<deferred>
## Deferred Ideas

- State machine configuration (Phase 2)
- State transitions and history (Phase 2)
- Kafka event publishing (Phase 3)
- GraphQL API (explicitly out of scope for v1)
- Multi-tenancy (out of scope)

</deferred>

---

*Phase: 01-entity-foundation*
*Context gathered: 2026-03-01*
