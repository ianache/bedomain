# Phase 3: Event Publishing - Context

**Gathered:** 2026-03-03
**Status:** Ready for planning

<domain>
## Phase Boundary

System publishes business events to Kafka for downstream consumer integration. Events are published when entity instances are created, updated, or when state transitions occur. The application connects to Kafka for event publishing.

</domain>

<decisions>
## Implementation Decisions

### Event Format
- **CloudEvents** — Industry standard JSON envelope with specversion, type, source, id, data
- Includes: eventId, timestamp, correlationId, source
- Data payload contains: entityId, entityType, attributes, previousState, newState, user

### Topic Design
- **Domain-based topics** — `bedomain.{domain}.{event}`
- Examples: `bedomain.entities.created`, `bedomain.entities.updated`, `bedomain.entities.state-changed`
- Consumer subscribes to `bedomain.entities.*` for all entity events

### Delivery Guarantee
- **Fire-and-forget** — Send to Kafka without waiting for ACK
- Non-blocking, minimal latency impact on API
- Best paired with log-and-continue error handling

### Error Handling
- **Log and continue** — If Kafka fails, log the error and proceed with the operation
- Requires monitoring/alerting for failed events
- Entity save succeeds even if event publishing fails

### Publishing
- **Asynchronous** — Return immediately after sending to Kafka
- No waiting for broker confirmation
- Combined with fire-and-forget delivery

</decisions>

<specifics>
## Specific Ideas

- CloudEvents structure:
  ```json
  {
    "specversion": "1.0",
    "type": "com.bedomain.entity.created",
    "source": "/bedomain",
    "id": "uuid",
    "time": "2026-03-03T12:00:00Z",
    "data": {
      "entityId": "...",
      "entityType": "Product",
      "attributes": {...},
      "user": "user-id"
    }
  }
  ```

</specifics>

<code_context>
## Existing Code Insights

### Reusable Assets
- EntityInstanceService.java — where entity create/update happens, event publishing should be hooked here
- StateTransitionService.java — where state changes occur, event publishing for state-changed
- Existing services use @Transactional and JwtAuthenticationService for audit

### Established Patterns
- Service layer pattern with @Transactional
- DTOs for responses
- Soft delete with deleted flag

### Integration Points
- EntityInstanceService.create() — publish ENTITY_CREATED event
- EntityInstanceService.update() — publish ENTITY_UPDATED event  
- StateTransitionService.triggerTransition() — publish STATE_CHANGED event

</code_context>

<deferred>
## Deferred Ideas

- None — discussion stayed within phase scope

</deferred>

---

*Phase: 03-event-publishing*
*Context gathered: 2026-03-03*
