# Requirements: bedomain

**Defined:** 2026-03-01
**Core Value:** Centralized management of business entity definitions with declarative state transition rules, full audit history, and event-driven integration patterns for reactive downstream processing.

## v1 Requirements

Requirements for initial release. Each maps to roadmap phases.

### Entity Type Management

- [x] **ETYP-01**: User can create entity type with name and description
- [x] **ETYP-02**: User can list all entity types
- [x] **ETYP-03**: User can get entity type by ID
- [x] **ETYP-04**: User can update entity type
- [x] **ETYP-05**: User can delete entity type

### Property Specifications

- [x] **PROP-01**: User can add property specification to entity type (name, description, data type)
- [x] **PROP-02**: User can list property specifications for an entity type
- [x] **PROP-03**: User can update property specification
- [x] **PROP-04**: User can delete property specification
- [x] **PROP-05**: Property specifications support domain data types (STRING, NUMBER, DATE, BOOLEAN, TEXT)

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

- [x] **EVNT-01**: System publishes business event to Kafka on state transition
- [x] **EVNT-02**: System publishes business event to Kafka on entity creation
- [x] **EVNT-03**: System publishes business event to Kafka on entity update
- [x] **EVNT-04**: Event includes entity ID, type, previous state, new state, timestamp, user

### Authentication & Security

- [ ] **AUTH-01**: API validates JWT token from Keycloak
- [ ] **AUTH-02**: All endpoints require valid JWT (except health check)
- [ ] **AUTH-03**: User context extracted from JWT for audit trail

### Infrastructure

- [x] **INFRA-01**: Application connects to MySQL database
- [x] **INFRA-02**: Application connects to Redis for caching
- [x] **INFRA-03**: Application connects to Kafka for event publishing
- [x] **INFRA-04**: Application health endpoint available

### Testing (Required for All Phases)

All new phases must include JUnit tests with minimum 80% code coverage.

- [ ] **TEST-01**: Phase 2 includes unit tests for state machine services
- [ ] **TEST-02**: Phase 2 includes unit tests for state machine controllers
- [ ] **TEST-03**: Phase 2 achieves 80%+ test coverage
- [ ] **TEST-04**: Phase 3 includes unit tests for Kafka event publishing
- [ ] **TEST-05**: Phase 3 achieves 80%+ test coverage

### State Hooks (JavaScript Execution)

- [x] **HOOK-01**: State can have onEnter JavaScript code that executes when entering the state
- [x] **HOOK-02**: State can have onExit JavaScript code that executes when exiting the state
- [x] **HOOK-03**: Script has access to entity attributes (read/write)
- [x] **HOOK-04**: Script execution is sandboxed for security
- [x] **HOOK-05**: Script timeout prevents infinite loops
- [x] **HOOK-06**: Script errors are logged and optionally block transition
- [x] **HOOK-07**: Execution audit trail is maintained

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

## Testing Standards

All future phases must include:

1. **Unit Tests**: JUnit 5 with Mockito for all service and controller classes
2. **Coverage Target**: Minimum 80% line coverage (services + controllers)
3. **Test Naming**: {ClassName}Test with descriptive test method names
4. **Test Dependencies**: H2 in-memory database for integration tests
5. **Coverage Tool**: JaCoCo Maven plugin

## Traceability

Which phases cover which requirements. Updated during roadmap creation.

| Requirement | Phase | Status |
|-------------|-------|--------|
| ETYP-01 | Phase 1 | Complete |
| ETYP-02 | Phase 1 | Complete |
| ETYP-03 | Phase 1 | Complete |
| ETYP-04 | Phase 1 | Complete |
| ETYP-05 | Phase 1 | Complete |
| PROP-01 | Phase 1 | Complete |
| PROP-02 | Phase 1 | Complete |
| PROP-03 | Phase 1 | Complete |
| PROP-04 | Phase 1 | Complete |
| PROP-05 | Phase 1 | Complete |
| ENTY-01 | Phase 1 | Complete |
| ENTY-02 | Phase 1 | Complete |
| ENTY-03 | Phase 1 | Complete |
| ENTY-04 | Phase 1 | Complete |
| ENTY-05 | Phase 1 | Complete |
| ENTY-06 | Phase 1 | Complete |
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
| TEST-01 | Phase 2 | Pending |
| TEST-02 | Phase 2 | Pending |
| TEST-03 | Phase 2 | Pending |
| EVNT-01 | Phase 3 | Complete |
| EVNT-02 | Phase 3 | Complete |
| EVNT-03 | Phase 3 | Complete |
| EVNT-04 | Phase 3 | Complete |
| TEST-04 | Phase 3 | Pending |
| TEST-05 | Phase 3 | Pending |
| AUTH-01 | Phase 1 | Complete |
| AUTH-02 | Phase 1 | Complete |
| AUTH-03 | Phase 1 | Complete |
| INFRA-01 | Phase 1 | Complete |
| INFRA-02 | Phase 1 | Complete |
| INFRA-03 | Phase 3 | Complete |
| INFRA-04 | Phase 1 | Complete |
| HOOK-01 | Phase 4 | Complete |
| HOOK-02 | Phase 4 | Complete |
| HOOK-03 | Phase 4 | Complete |
| HOOK-04 | Phase 4 | Complete |
| HOOK-05 | Phase 4 | Complete |
| HOOK-06 | Phase 4 | Complete |
| HOOK-07 | Phase 4 | Complete |

**Coverage:**
- v1 requirements: 39 total
- Testing requirements: 5
- Hook requirements: 7
- Mapped to phases: 51
- Unmapped: 0 ✓

---
*Requirements defined: 2026-03-01*
*Last updated: 2026-03-01 after initial definition*
