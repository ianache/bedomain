# Context: Phase 4 - State Hooks

## User Request

"Necesito incorporar la capacidad para que cuando se produzca una entrada o salida de cada estado se pueda ejecutar código JavaScript/TypeScript"

## Functional Requirements

Each state can have JavaScript/TypeScript code that executes:
- **onEnter**: when entering the state
- **onExit**: when exiting the state

The code must:
- Access entity context (current attributes)
- Be able to modify attributes before completing the transition
- Be secure (sandboxed execution)

## Design Considerations (from user)

1. **Script storage**: In StateSpec or separate table?
2. **Execution engine**: GraalJS (Polyglot), Nashorn (deprecated), or external service?
3. **Security**: Sandboxing for user code
4. **Timeout**: Code must not block indefinitely
5. **Error handling**: What happens if script fails? Block transition?
6. **Context**: What entity data can scripts access?
7. **Logging/Auditing**: Record hook execution

## Decisions Made

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Script storage | StateSpec (onEnter/onExit columns) | Simpler than separate table, logical co-location |
| Engine | GraalJS | Modern, Oracle-maintained, sandbox support |
| Sandbox policy | CONSTRAINED | Good balance of security and usability |
| Timeout | 2 seconds | Reasonable for most scripts |
| Error handling | Configurable (failOnError=true default) | Allow flexibility per use case |
| Context | entity global object with attributes | Simple JavaScript API |

## Existing Architecture to Build On

- Spring Boot 3.2.0 with Java 21
- JPA/Hibernate with MySQL
- StateTransitionService.triggerTransition() is the integration point
- StateHistory already tracks transitions (will extend for hook audit)

## Dependencies on Prior Work

- Phase 2: StateMachine, StateSpec, TransitionSpec entities
- Phase 2: StateTransitionService.triggerTransition() method
- Phase 3: Event publishing (hooks execute before events)

## Non-Goals (Deferred)

- TypeScript execution (would require transpilation)
- Script library/imports
- Visual script editor UI
- Script versioning

---

*Context created: 2026-03-08*
*Based on user requirements and design discussion*
