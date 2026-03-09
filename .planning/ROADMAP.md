# Roadmap: bedomain

## Overview

A SpringBoot microservice for managing business entity definitions with configurable state machines, event-driven architecture, and JWT authentication. The journey progresses from foundational entity management through state machine logic to event publishing for downstream consumers.

## Phases

**Phase Numbering:**
- Integer phases (1, 2, 3, 4): Planned milestone work

- [x] **Phase 1: Entity Foundation** - Entity types, properties, instances, authentication, and core infrastructure (completed 2026-03-02)
- [x] **Phase 2: State Machine Core** - State machine configuration, transitions, and history tracking (completed 2026-03-03)
- [x] **Phase 3: Event Publishing** - Kafka event publishing for downstream integration (completed 2026-03-03)
- [x] **Phase 4: State Hooks** - JavaScript execution for onEnter/onExit state hooks (completed 2026-03-08)
- [ ] **Phase 5: GitLab Integration** - Hook JavaScript para consumir API de GitLab (Test Case Demo)

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

### Phase 4: State Hooks
**Goal**: Execute JavaScript code on state entry/exit for business logic customization
**Depends on**: Phase 3
**Requirements**: HOOK-01, HOOK-02, HOOK-03, HOOK-04, HOOK-05, HOOK-06, HOOK-07
**Success Criteria** (what must be TRUE):
  1. State can have onEnter JavaScript code that executes when entering the state
  2. State can have onExit JavaScript code that executes when exiting the state
  3. Script has access to entity attributes (read/write)
  4. Script execution is sandboxed for security
  5. Script timeout prevents infinite loops
  6. Script errors are logged and optionally block transition
  7. Execution audit trail is maintained in StateHistory

### Phase 5: GitLab Integration
**Goal**: Demonstrar uso de hooks JavaScript para consumir API de GitLab al cerrar un Issue
**Depends on**: Phase 4
**Requirements**: HOOK-01, HOOK-02 (existing, reusing)
**Success Criteria** (what must be TRUE):
  1. Script JavaScript puede consumir API de GitLab desde onEnter hook
  2. Script usa entity.issueId para identificar el issue
  3. Script añade label "Bug :: Developer" al issue
  4. Script maneja errores apropiadamente
  5. Timeout de 2 segundos se respeta

## Progress

**Execution Order:**
Phases execute in numeric order: 1 → 2 → 3

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Entity Foundation | 5/5 | Complete    | 2026-03-02 |
| 2. State Machine Core | 2/2 | Complete    | 2026-03-03 |
| 3. Event Publishing | 3/3 | Complete    | 2026-03-03 |
| 4. State Hooks | 4/4 | Complete    | 2026-03-08 |

**Phase 1 Plans:**
- [x] 01-01-PLAN.md — Infrastructure & Security Setup (2026-03-01)
- [x] 01-02a-PLAN.md — Domain Entities, DTOs, Repositories (2026-03-01)
- [x] 01-02b-PLAN.md — Entity Type & Property Management API (2026-03-01)
- [x] 01-03-PLAN.md — Entity Instance Management API (2026-03-02)
- [x] 01-04-PLAN.md — Gap Closure: Package Consolidation (2026-03-02)

**Phase 2 Plans:**
- [x] 02-01-PLAN.md — State Machine Configuration & CRUD APIs (2026-03-03)
- [x] 02-02-PLAN.md — State Transitions & History (2026-03-03)

**Phase 3 Plans:**
- [x] 03-01-PLAN.md — Kafka Infrastructure Setup (2026-03-03)
- [x] 03-02-PLAN.md — Event Publisher Service (2026-03-03)
- [x] 03-03-PLAN.md — Event Integration (2026-03-03)

**Phase 4 Plans:**
- [x] 04-01-PLAN.md — State Script Fields (Model + DTOs)
- [x] 04-02-PLAN.md — JavaScript Executor Engine (GraalJS)
- [x] 04-03-PLAN.md — Hook Integration in StateTransition
- [x] 04-04-PLAN.md — Unit Tests (TDD)

---

*Roadmap created: 2026-03-01*
*Based on v1 requirements (39 total)*
