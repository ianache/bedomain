# Requirements: bedomain

**Defined:** 2026-03-01
**Core Value:** Centralized management of business entity definitions with declarative state transition rules, full audit history, and event-driven integration patterns for reactive downstream processing.

## v1 Requirements

Requirements for initial release. Each maps to roadmap phases.

### Entity Type Management

- [ ] **ETYP-01**: User can create entity type with name and description
- [ ] **ETYP-02**: User can list all entity types
- [ ] **ETYP-03**: User can get entity type by ID
- [ ] **ETYP-04**: User can update entity type
- [ ] **ETYP-05**: User can delete entity type

### Property Specifications

- [ ] **PROP-01**: User can add property specification to entity type (name, description, data type)
- [ ] **PROP-02**: User can list property specifications for an entity type
- [ ] **PROP-03**: User can update property specification
- [ ] **PROP-04**: User can delete property specification
- [ ] **PROP-05**: Property specifications support domain data types (STRING, NUMBER, DATE, BOOLEAN, TEXT)

### Entity Instance Management

- [ ] **ENTY-01**: User can create entity instance with type reference and initial attributes
- [ ] **ENTY-02**: User can list entity instances (with filtering by type and state)
- [ ] **ENTY-03**: User can get entity instance by ID
- [ ] **ENTY-04**: User can update entity instance attributes
- [ ] **ENTY-05**: User can delete entity instance
- [ ] **ENTY-06**: Entity instance stores id (GUID), creation date, created by

### State Machine Management

- [ ] **SM-01**: User can create state machine for entity type
- [ ] **SM-02**: User can define state specifications (name, description)
- [ ] **SM-03**: User can define state transition specifications (from state, to state)
- [ ] **SM-04**: User can list state machines
- [ ] **SM-05**: User can get state machine by ID with states and transitions
- [ ] **SM-06**: User can update state machine configuration

### State Transitions

- [ ] **TRAN-01**: User can trigger state transition on entity instance
- [ ] **TRAN-02**: System validates transition is allowed per state machine rules
- [ ] **TRAN-03**: System rejects invalid state transitions with error
- [ ] **TRAN-04**: Transition creates state history record

### State History

- [ ] **HIST-01**: User can view state history for entity instance
- [ ] **HIST-02**: History includes from state, to state, transition date, user
- [ ] **HIST-03**: History includes current state snapshot

### Business Events

- [ ] **EVNT-01**: System publishes business event to Kafka on state transition
- [ ] **EVNT-02**: System publishes business event to Kafka on entity creation
- [ ] **EVNT-03**: System publishes business event to Kafka on entity update
- [ ] **EVNT-04**: Event includes entity ID, type, previous state, new state, timestamp, user

### Authentication & Security

- [ ] **AUTH-01**: API validates JWT token from Keycloak
- [ ] **AUTH-02**: All endpoints require valid JWT (except health check)
- [ ] **AUTH-03**: User context extracted from JWT for audit trail

### Infrastructure

- [ ] **INFRA-01**: Application connects to MySQL database
- [ ] **INFRA-02**: Application connects to Redis for caching
- [ ] **INFRA-03**: Application connects to Kafka for event publishing
- [ ] **INFRA-04**: Application health endpoint available

## v2 Requirements

Deferred to future release. Tracked but not in current roadmap.

### Caching

- **CACHE-01**: Redis caching layer for entity type lookups
- **CACHE-02**: Redis caching for frequently accessed entity instances
- **CACHE-03**: Cache invalidation on entity updates

### Advanced Features

- **ADV-01**: Property-level validation rules (regex, range)
- **ADV-02**: Advanced query/filtering capabilities
- **ADV-03**: Business event type management

## Out of Scope

Explicitly excluded. Documented to prevent scope creep.

| Feature | Reason |
|---------|--------|
| GraphQL API | REST API sufficient for v1, per architecture spec |
| Multi-tenancy | Single tenant for v1 |
| Complex validation rules engine | State machine transitions sufficient for v1 |
| Entity versioning | State history provides audit trail |
| Web UI | API-first, consumer services build UIs |
| Real-time WebSocket updates | Complexity not needed for v1 |

## Traceability

Which phases cover which requirements. Updated during roadmap creation.

| Requirement | Phase | Status |
|-------------|-------|--------|
| ETYP-01 | Phase 1 | Pending |
| ETYP-02 | Phase 1 | Pending |
| ETYP-03 | Phase 1 | Pending |
| ETYP-04 | Phase 1 | Pending |
| ETYP-05 | Phase 1 | Pending |
| PROP-01 | Phase 1 | Pending |
| PROP-02 | Phase 1 | Pending |
| PROP-03 | Phase 1 | Pending |
| PROP-04 | Phase 1 | Pending |
| PROP-05 | Phase 1 | Pending |
| ENTY-01 | Phase 1 | Pending |
| ENTY-02 | Phase 1 | Pending |
| ENTY-03 | Phase 1 | Pending |
| ENTY-04 | Phase 1 | Pending |
| ENTY-05 | Phase 1 | Pending |
| ENTY-06 | Phase 1 | Pending |
| SM-01 | Phase 2 | Pending |
| SM-02 | Phase 2 | Pending |
| SM-03 | Phase 2 | Pending |
| SM-04 | Phase 2 | Pending |
| SM-05 | Phase 2 | Pending |
| SM-06 | Phase 2 | Pending |
| TRAN-01 | Phase 2 | Pending |
| TRAN-02 | Phase 2 | Pending |
| TRAN-03 | Phase 2 | Pending |
| TRAN-04 | Phase 2 | Pending |
| HIST-01 | Phase 2 | Pending |
| HIST-02 | Phase 2 | Pending |
| HIST-03 | Phase 2 | Pending |
| EVNT-01 | Phase 3 | Pending |
| EVNT-02 | Phase 3 | Pending |
| EVNT-03 | Phase 3 | Pending |
| EVNT-04 | Phase 3 | Pending |
| AUTH-01 | Phase 1 | Pending |
| AUTH-02 | Phase 1 | Pending |
| AUTH-03 | Phase 1 | Pending |
| INFRA-01 | Phase 1 | Pending |
| INFRA-02 | Phase 1 | Pending |
| INFRA-03 | Phase 3 | Pending |
| INFRA-04 | Phase 1 | Pending |

**Coverage:**
- v1 requirements: 39 total
- Mapped to phases: 39
- Unmapped: 0 ✓

---
*Requirements defined: 2026-03-01*
*Last updated: 2026-03-01 after initial definition*
