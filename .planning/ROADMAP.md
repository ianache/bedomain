# Roadmap: bedomain

## Overview

A SpringBoot microservice for managing business entity definitions with configurable state machines, event-driven architecture, and JWT authentication. The journey progresses from foundational entity management through state machine logic to event publishing for downstream consumers.

## Phases

**Phase Numbering:**
- Integer phases (1, 2, 3): Planned milestone work

- [ ] **Phase 1: Entity Foundation** - Entity types, properties, instances, authentication, and core infrastructure
- [ ] **Phase 2: State Machine Core** - State machine configuration, transitions, and history tracking
- [ ] **Phase 3: Event Publishing** - Kafka event publishing for downstream integration

## Phase Details

### Phase 1: Entity Foundation
**Goal**: Users can define entity types with properties, create entity instances, and authenticate via Keycloak JWT
**Depends on**: Nothing (first phase)
**Requirements**: ETYP-01, ETYP-02, ETYP-03, ETYP-04, ETYP-05, PROP-01, PROP-02, PROP-03, PROP-04, PROP-05, ENTY-01, ENTY-02, ENTY-03, ENTY-04, ENTY-05, ENTY-06, AUTH-01, AUTH-02, AUTH-03, INFRA-01, INFRA-02, INFRA-04
**Success Criteria** (what must be TRUE):
  1. User can create, list, get, update, and delete entity types via REST API
  2. User can add, list, update, and delete property specifications with domain data types (STRING, NUMBER, DATE, BOOLEAN, TEXT)
  3. User can create, list, get, update, and delete entity instances with type reference and attributes
  4. All API endpoints require valid JWT token from Keycloak (except health check)
  5. Application connects to MySQL, Redis, and exposes health endpoint

### Phase 2: State Machine Core
**Goal**: Users can configure state machines, trigger valid state transitions on entities, and view history
**Depends on**: Phase 1
**Requirements**: SM-01, SM-02, SM-03, SM-04, SM-05, SM-06, TRAN-01, TRAN-02, TRAN-03, TRAN-04, HIST-01, HIST-02, HIST-03
**Success Criteria** (what must be TRUE):
  1. User can create, list, get, and update state machines with states and transition rules
  2. User can trigger state transitions on entity instances
  3. System validates transitions against state machine rules and rejects invalid transitions
  4. Each transition creates a state history record with from/to state, date, and user
  5. User can view complete state history for any entity instance

### Phase 3: Event Publishing
**Goal**: System publishes business events to Kafka for downstream consumer integration
**Depends on**: Phase 2
**Requirements**: EVNT-01, EVNT-02, EVNT-03, EVNT-04, INFRA-03
**Success Criteria** (what must be TRUE):
  1. System publishes business event to Kafka topic when entity instance is created
  2. System publishes business event to Kafka topic when entity instance is updated
  3. System publishes business event to Kafka topic when state transition occurs
  4. Event payload includes entity ID, type, previous state, new state, timestamp, and user
  5. Application connects to Kafka for event publishing

## Progress

**Execution Order:**
Phases execute in numeric order: 1 → 2 → 3

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Entity Foundation | 2/3 | In Progress | - |
| 2. State Machine Core | 0/2 | Not started | - |
| 3. Event Publishing | 0/2 | Not started | - |

**Phase 1 Plans:**
- [x] 01-01-PLAN.md — Infrastructure & Security Setup (2026-03-01)
- [x] 01-02a-PLAN.md — Domain Entities, DTOs, Repositories (2026-03-01)
- [ ] 01-02b-PLAN.md — Entity Type & Property Management API
- [ ] 01-03-PLAN.md — Entity Instance Management API

---

*Roadmap created: 2026-03-01*
*Based on v1 requirements (39 total)*
