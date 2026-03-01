# Pitfalls Research

**Domain:** Business Entities Management Domain Service (bedomain)
**Researched:** 2026-03-01
**Confidence:** MEDIUM

## Critical Pitfalls

### Pitfall 1: State Machine Transitions Bypass Validation

**What goes wrong:**
Entity state transitions happen without validating against the defined state machine rules. Entity moves from `DRAFT` to `ACTIVE` even though the transition isn't allowed per the state machine configuration.

**Why it happens:**
- State machine configuration stored separately from entity logic
- No atomic transaction between state change and rule validation
- Stateful entity loaded, rules changed mid-flight between validation and save

**How to avoid:**
- Load state machine rules within the same transaction as entity state change
- Use optimistic locking with version field on entity state
- Implement state transition as single atomic operation with built-in guard conditions

**Warning signs:**
- Race conditions appearing in state transitions under load
- Entities in impossible states (e.g., COMPLETED without ever being ACTIVE)
- State history showing inconsistent transition sequences

**Phase to address:** Phase 2 - State Machine Core Implementation

---

### Pitfall 2: Dual-Write Problem with Kafka Events

**What goes wrong:**
Entity state is persisted to MySQL but Kafka event fails to publish (network issue, broker down). Downstream services miss critical state changes, creating inconsistent views.

**Why it happens:**
- DB write succeeds, Kafka publish fails silently or after commit
- No transactional outbox pattern implemented
- Event published before DB commit (causes rollback inconsistency)

**How to avoid:**
- Implement Transactional Outbox Pattern: write events to outbox table within same DB transaction
- Use Kafka connector to poll outbox and publish atomically
- Never publish event before DB transaction commits

**Warning signs:**
- Downstream services reporting missing state transitions
- Event sequence gaps in consumer logs
- Manual reprocessing scripts running frequently

**Phase to address:** Phase 3 - Event Publishing

---

### Pitfall 3: Cached Entity State Becomes Stale After Transition

**What goes wrong:**
Redis cache returns old entity state after state transition completes. Client reads cached data, sees wrong state, takes incorrect actions.

**Why it happens:**
- Cache invalidated only on read, not on state transition write
- Multiple cache entries for same entity (different keys)
- TTL too long for frequently transitioning entities

**How to avoid:**
- Invalidate cache as part of state transition write operation
- Use cache-aside pattern with write-through invalidation
- Consider shorter TTLs for entities with frequent transitions (configurable per entity type)

**Warning signs:**
- Clients reporting "impossible" states (entity in two states simultaneously)
- Redis cache keys with different values than MySQL
- Users complaining about "stuck" entities

**Phase to address:** Phase 4 - Caching Layer

---

### Pitfall 4: Entity Type Configuration Drift

**What goes wrong:**
Entity type definition is modified (states/transitions removed) but existing entity instances still reference old configuration. New transitions fail, old entities become unprocessable.

**Why it happens:**
- Entity type schema version not tracked
- Existing entities linked to soft-deleted or modified configurations
- No migration strategy for existing entities when type changes

**How to avoid:**
- Implement versioning for entity type configurations
- Entity instances reference specific version, not latest
- Deprecation workflow: mark transitions as deprecated before removal
- Generate migration scripts for entity instances when type changes

**Warning signs:**
- Entities failing to transition with "invalid transition" errors
- Multiple entity type versions in production with no clear strategy
- Unable to delete entity types due to existing instances

**Phase to address:** Phase 1 - Entity Type Management

---

### Pitfall 5: JWT Validation Performance Degradation

**What goes wrong:**
Every API request triggers remote Keycloak introspection or JWKS fetch. Latency spikes under load, Keycloak becomes bottleneck, service unavailable when Keycloak is down.

**Why it happens:**
- No local JWT validation with cached public keys
- Remote introspection on every request
- JWKS cache TTL too short or not cached

**How to avoid:**
- Use local JWT validation with JWKS cached in memory
- Set reasonable JWKS cache TTL (hours, not minutes)
- Implement token caching at gateway level
- Only fallback to introspection for refresh tokens

**Warning signs:**
- API latency correlating with Keycloak response time
- Timeouts during Keycloak maintenance windows
- High network I/O to Keycloak server

**Phase to address:** Phase 2 - Authentication

---

### Pitfall 6: Event Payload Mismatch with State

**What goes wrong:**
Kafka event contains state that doesn't match actual entity state (event published before final DB commit, or published with stale data). Consumers act on incorrect information.

**Why it happens:**
- Event published with optimistic state assumption
- State transition rolls back but event already sent
- Event built from request data, not verified with actual entity

**How to avoid:**
- Always publish event AFTER successful DB commit
- Include state history timestamp in event payload
- Consumers must validate current state before acting, not trust event blindly

**Warning signs:**
- Consumers throwing exceptions on event processing
- Event replay showing inconsistencies
- Dual-write detection tools flagging mismatches

**Phase to address:** Phase 3 - Event Publishing

---

## Technical Debt Patterns

| Shortcut | Immediate Benefit | Long-term Cost | When Acceptable |
|----------|-------------------|----------------|-----------------|
| Skip state history for "simple" entities | Faster initial development | No audit trail, regulatory compliance risk | Never |
| Hardcode state transitions in code | Simple to implement | Any change requires redeployment | Never for v1 |
| Skip optimistic locking | Simpler transaction code | Lost updates, race conditions | Only for read-only entities |
| Publish events synchronously | Easier to debug | Request latency, cascade failures | Only for non-critical events |
| Single outbox table for all events | Less initial setup | Schema grows complex, harder to scale | Only for v1 MVP |

---

## Integration Gotchas

| Integration | Common Mistake | Correct Approach |
|-------------|----------------|------------------|
| Keycloak | Not handling token refresh gracefully | Implement token refresh interceptor, handle 401 with retry |
| Kafka | No idempotency handling | Design consumers for duplicate events (use event IDs) |
| Redis | Using default serializer (Java) | Use JSON/JSONB for cross-platform compatibility |
| MySQL | Missing composite indexes on entity queries | Index (entityType, state), (entityType, createdAt) |

---

## Performance Traps

| Trap | Symptoms | Prevention | When It Breaks |
|------|----------|------------|----------------|
| N+1 queries for entity properties | DB CPU spikes on list endpoints | Use JOIN FETCH or batch queries | >100 entities per request |
| Unbounded state history queries | Memory exhaustion on large entities | Paginate history, limit default page size | >1000 transitions |
| Cache stampede on hot entities | DB spikes when cache expires | Implement cache-aside with locking | High-traffic entity types |
| No query result limits | OOM on large result sets | Enforce max page size, reject unbounded queries | Any production load |

---

## Security Mistakes

| Mistake | Risk | Prevention |
|---------|------|------------|
| Not validating entity access beyond JWT | Users can access entities they don't own | Add ownership/tenant checks in service layer |
| Storing sensitive properties without encryption | Data leak if DB compromised | Encrypt sensitive fields at application level |
| Allowing entity type creation without approval | Chaos from unconstrained entity definitions | RBAC: restrict entity type CRUD to admins |
| Missing rate limiting on state transitions | DoS via rapid state changes | Add rate limiting per entity per user |

---

## UX Pitfalls

| Pitfall | User Impact | Better Approach |
|---------|-------------|-----------------|
| No clear error on invalid transition | Users don't know why action failed | Return actionable error: "Cannot transition from DRAFT to COMPLETED. Valid: DRAFT->ACTIVE, ACTIVE->COMPLETED" |
| Hidden state requirements | Trial-and-error to find valid transitions | Expose state machine definition via API for UI rendering |
| Slow state transitions under load | Users think operation failed, retry | Return immediate acceptance, async processing with status endpoint |

---

## "Looks Done But Isn't" Checklist

- [ ] **State Machine:** Transitions defined but not validated at write time — verify with integration test
- [ ] **Kafka Events:** Events publish but no idempotency handling — verify consumer handles duplicates
- [ ] **Redis Cache:** Cache works but doesn't invalidate on state change — verify with live state transition
- [ ] **JWT Auth:** Auth works but validates remotely every request — verify with network inspection under load
- [ ] **Entity History:** History records but doesn't include who/what — verify full audit trail

---

## Recovery Strategies

| Pitfall | Recovery Cost | Recovery Steps |
|---------|---------------|----------------|
| Stale cache | LOW | Clear cache key, re-fetch from DB |
| Lost Kafka event | MEDIUM | Replay from event store, implement idempotent consumer |
| Invalid state transition | MEDIUM | Manually correct state, log remediation |
| JWT validation outage | LOW | Fallback to cached JWKS, fail open with alerting |

---

## Pitfall-to-Phase Mapping

| Pitfall | Prevention Phase | Verification |
|---------|------------------|--------------|
| State Machine Validation | Phase 2 - State Machine Core | Integration test: verify invalid transitions fail |
| Dual-Write Problem | Phase 3 - Event Publishing | End-to-end test: verify event after DB commit |
| Stale Cache | Phase 4 - Caching Layer | Manual test: transition entity, immediately read |
| Entity Type Drift | Phase 1 - Entity Type Management | Unit test: modify type, verify existing entities unaffected |
| JWT Performance | Phase 2 - Authentication | Load test: measure auth latency under load |
| Event Payload Mismatch | Phase 3 - Event Publishing | Integration test: compare event to DB state |

---

## Sources

- [Spring State Machine Error Handling](https://blog.stackademic.com/%EF%B8%8F-mastering-complex-workflows-with-spring-statemachine-the-hidden-superpower-for-error-handling-48e0354a57eb)
- [Event-Driven Architecture Pitfalls](https://www.linkedin.com/posts/raul-junco_most-event-driven-architecture-failures-arent-activity-7363179664221040641-WlOa)
- [Spring Boot Redis Caching Mistakes](https://medium.com/@himanshu675/10-dangerous-redis-caching-mistakes-in-spring-boot-and-how-to-fix-them-fast-2161554290c2)
- Security Mistakes](https [Spring Boot JWT://medium.com/@diyasanjaysatpute147/5-spring-boot-security-mistakes-with-jwt-oauth2-and-how-to-fix-them-fast-8ea1b18e8086)
- [Dual Write Problem](https://andrelucas.io/the-dual-write-problem-in-practice-spring-boot-kafka-and-postgresql-f77980e9ae0e)
- [Outbox Pattern with Kafka](https://medium.com/%40bharathdayals/building-a-fault-tolerant-kafka-event-processing-system-using-the-outbox-pattern-spring-boot-0b3a6500a064)

---

*Pitfalls research for: bedomain - Business Entities Management Domain Service*
*Researched: 2026-03-01*
