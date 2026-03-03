# Phase 2: State Machine Core - Research

**Researched:** 2026-03-02
**Domain:** Spring Boot State Machine Implementation
**Confidence:** HIGH

## Summary

Spring State Machine (SSM) 4.x is the de facto standard for state machine implementation in Spring Boot. The framework provides built-in JPA persistence support via `spring-statemachine-data-jpa`, enabling state machine configuration storage alongside business entities. For this phase, the pattern is: store state machine definitions (states, transitions) as configurable JPA entities, and use Spring State Machine to validate/apply transitions to EntityInstance records.

**Primary recommendation:** Use Spring State Machine 4.x with `@EnableStateMachineFactory`, store state machine config in custom JPA entities (not SSM's repository tables), and integrate via StateMachineRuntimePersister for atomic entity+state updates.

---

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| spring-statemachine-core | 4.0.0+ | State machine framework | Official Spring project, active maintenance |
| spring-statemachine-autoconfigure | 4.0.0+ | Auto-configuration | Boot starter integration |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| spring-statemachine-data-jpa | 4.0.0+ | JPA persistence for SM config | Only if storing SM definitions in DB (not required for v1) |
| Lombok | (from Phase 1) | Reduce boilerplate | All entities |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Spring State Machine | Custom enum + service layer | Faster initial dev, but no declarative transitions, harder to audit |
| Spring State Machine | jStateMachine (older) | Less community adoption, fewer features |
| DB-stored SM config | Hard-coded SM config | Flexibility for admin changes vs simpler implementation |

**Note:** For this phase, hard-coded state machine configurations (via `@Configuration`) are simpler and sufficient. DB-driven SM config can be added in Phase 3 if needed.

---

## Architecture Patterns

### Recommended Project Structure
```
src/main/java/com/bedomain/
├── config/
│   └── StateMachineConfig.java          # SSM configuration
├── domain/
│   ├── entity/
│   │   ├── StateMachine.java            # SM definition entity
│   │   ├── StateSpec.java                # State specification
│   │   ├── TransitionSpec.java          # Transition definition
│   │   └── StateHistory.java             # Audit/history record
│   ├── dto/statmachine/
│   │   ├── StateMachineResponse.java
│   │   ├── CreateStateMachineRequest.java
│   │   └── TriggerTransitionRequest.java
│   ├── enums/
│   │   └── EntityState.java              # Enum for entity states per type
│   └── repository/
│       ├── StateMachineRepository.java
│       ├── StateSpecRepository.java
│       ├── TransitionSpecRepository.java
│       └── StateHistoryRepository.java
├── service/
│   ├── StateMachineService.java         # CRUD for SM configs
│   ├── StateTransitionService.java      # Transition logic
│   └── StateHistoryService.java         # History queries
├── statemachine/
│   └── EntityStateMachineFactory.java    # SSM factory bean
└── controller/
    ├── StateMachineController.java      # SM CRUD endpoints
    └── EntityStateController.java        # Transition endpoints
```

### Pattern 1: State Machine Per Entity Type
**What:** Each EntityType has its own state machine configuration (states + transitions)
**When to use:** When different entity types have different workflows
**Example:**
```java
// Each entity instance references its type's state machine
@Entity
public class EntityInstance {
    @ManyToOne
    private EntityType entityType;
    
    private String currentState;  // References StateSpec.name
    
    @OneToMany(mappedBy = "entityInstance")
    private List<StateHistory> stateHistory;
}
```

### Pattern 2: StateMachineFactory with Actions
**What:** Use `@EnableStateMachineFactory` to create state machine instances per entity
**When to use:** Need stateful transitions with guards and actions
**Example:**
```java
@Configuration
@EnableStateMachineFactory
public class EntityStateMachineConfig extends StateMachineConfigurerAdapter<String, String> {
    
    @Override
    public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
        states
            .withStates()
            .initial("DRAFT")
            .states(EnumSet.allOf(EntityState.class))
            .end("DELETED");
    }
    
    @Override
    public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
        transitions
            .withExternal()
            .source("DRAFT").target("ACTIVE").event("ACTIVATE")
            .and()
            .withExternal()
            .source("ACTIVE").target("ARCHIVED").event("ARCHIVE");
    }
}
```

### Pattern 3: Service Layer Integration
**What:** Service layer orchestrates entity update + SM transition atomically
**When to Use:** Always - SM is the validation layer, service handles persistence
**Example:**
```java
@Service
public class StateTransitionService {
    
    private final StateMachineFactory<String, String> factory;
    private final EntityInstanceRepository entityRepo;
    
    public void transition(Long entityId, String event, String user) {
        EntityInstance entity = entityRepo.findById(entityId)
            .orElseThrow(() -> new EntityNotFoundException("Entity not found"));
            
        StateMachine<String, String> sm = factory.getStateMachine(entity.getId().toString());
        
        // Initialize from current state
        sm.stopReactively().block();
        sm.getStateMachineAccessor()
            .doWithAllRegions(accessor -> accessor.resetStateMachineReactively(
                new DefaultStateMachineContext<>(entity.getCurrentState(), null, null, null)
            )).block();
        sm.startReactively().block();
        
        // Send event - validates transition is allowed
        boolean accepted = sm.sendEvent(event);
        if (!accepted) {
            throw new InvalidTransitionException("Transition not allowed");
        }
        
        // Update entity with new state
        entity.setCurrentState(sm.getState().getId());
        entityRepo.save(entity);
        
        // Record history
        historyService.record(entity, event, user);
    }
}
```

### Anti-Patterns to Avoid
- **Storing SM state in ExtendedState only:** Never persist state only in SM extended state - must sync to JPA entity for durability
- **Creating new StateMachine per request without reuse:** StateMachine construction is expensive; use factory pattern
- **Ignoring transaction boundaries:** State machine transition + entity update must be atomic

---

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| State transition validation | Custom if/else checks | Spring State Machine | Declarative, enforced, auditable |
| Transition guards | Hard-coded conditions | SSM Guard interface | Reusable, testable, SpEL support |
| State history tracking | Custom audit tables | Dedicated StateHistory entity | Standard pattern, queryable |
| State machine persistence | Custom serialization | SSM StateMachinePersister | Handles hierarchy, contexts |

**Key insight:** State machines are deceptively complex. What looks like simple "if from=A and to=B" logic explodes with guards, actions, hierarchical states, error handling, and concurrency. Spring State Machine handles all of this with extensive testing.

---

## Common Pitfalls

### Pitfall 1: State Not Persisted to Entity
**What goes wrong:** SM transitions, but entity state remains unchanged
**Why it happens:** Forgetting to sync SM state back to JPA entity after transition
**How to avoid:** Always update entity in the same transaction as SM event; use actions or explicit save
**Warning signs:** Entity shows old state after successful API call

### Pitfall 2: Race Conditions in Concurrent Updates
**What goes wrong:** Two requests modify entity state simultaneously, one overwrites the other
**Why it happens:** No optimistic locking on entity + SM not designed for concurrent access
**How to avoid:** Use `@Version` for optimistic locking; implement idempotency keys for retry-safe operations
**Warning signs:** Intermittent state inconsistencies under load

### Pitfall 3: State Machine Not Rehydrated from Database
**What goes wrong:** New SM instance starts at initial state, ignoring persisted state
**Why fresh it happens:** Creating SM without loading existing state via StateMachineContext
**How to avoid:** Use `StateMachineContext` to restore state from entity before sending events
**Warning signs:** All transitions work from initial state only

### Pitfall 4: Error Handling in Actions Fails Silently
**What goes wrong:** Action throws exception but SM reports transition as successful
**Why it happens:** `sendEvent()` returns true if event accepted, not if action succeeded
**How to avoid:** Use `StateMachineInterceptor` to catch errors; wrap action errors in StateContext
**Warning signs:** API returns success but business logic failed

### Pitfall 5: Complex Hierarchical States with Persistence Issues
**What goes wrong:** Deeply nested states don't restore correctly from DB
**Why it happens:** Known limitation with 4+ level hierarchies in SSM persistence
**How to avoid:** Keep hierarchy to 2-3 levels max; test restore thoroughly
**Warning signs:** State restores to parent instead of leaf state

---

## Code Examples

### State History Entity
```java
@Entity
@Table(name = "state_history")
public class StateHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "entity_instance_id")
    private EntityInstance entityInstance;
    
    private String fromState;
    private String toState;
    private String event;
    private String triggeredBy;
    private Instant timestamp;
    
    // Getters, setters
}
```

### Transition Service with Error Handling
```java
@Service
@RequiredArgsConstructor
public class StateTransitionService {
    
    private final StateMachineFactory<String, String> stateMachineFactory;
    private final EntityInstanceRepository entityRepository;
    private final StateHistoryService historyService;
    
    @Transactional
    public EntityInstance triggerTransition(Long entityId, String event, String user) {
        EntityInstance entity = entityRepository.findById(entityId)
            .orElseThrow(() -> new EntityNotFoundException("Entity not found"));
            
        StateMachine<String, String> sm = stateMachineFactory.getStateMachine(entityId.toString());
        
        sm.stopReactively().block();
        sm.getStateMachineAccessor()
            .doWithAllRegions(accessor -> accessor.resetStateMachineReactively(
                new DefaultStateMachineContext<>(entity.getCurrentState(), null, null, null)
            )).block();
        sm.startReactively().block();
        
        boolean accepted = sm.sendEvent(event);
        
        if (!accepted || sm.hasStateChanged()) {
            throw new InvalidTransitionException(
                "Cannot transition from " + entity.getCurrentState() + " with event " + event);
        }
        
        String newState = sm.getState().getId();
        entity.setCurrentState(newState);
        EntityInstance saved = entityRepository.save(entity);
        
        historyService.record(entity, entity.getCurrentState(), newState, event, user);
        
        return saved;
    }
}
```

---

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Hard-coded enums for states | SSM with configurable states | 2020+ | Allows runtime SM changes |
| Manual transition validation | SSM Guards + Transitions | 2016+ | Declarative, testable |
| No state persistence | StateMachinePersister | 2016+ | Stateful SM across restarts |
| Monolithic SM config | StateMachineFactory | 2017+ | Instance-per-entity pattern |

**Deprecated/outdated:**
- `@EnableStateMachine` (singleton) - Use `@EnableStateMachineFactory` for per-entity instances
- XML configuration - Java config is now standard

---

## Open Questions

1. **Should SM configuration be DB-driven or code-driven?**
   - What we know: Requirements say CRUD for state machines (SM-01 to SM-06)
   - What's unclear: Whether state machine definitions need to be runtime-editable
   - Recommendation: Start with code-driven (SSM config classes), add DB-driven if Phase 3 requires admin UI

2. **How to handle multiple entity types with different states?**
   - What we know: Each EntityType can have its own SM
   - What's unclear: Single SM factory vs per-type factories
   - Recommendation: Single factory with state/event as Strings, look up transitions per entity's type

---

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit 5 + Mockito (existing from Phase 1) |
| Config file | pom.xml with spring-boot-starter-test |
| Quick run command | `mvn test -Dtest="*StateMachine*Test" -q` |
| Full suite command | `mvn test -q` |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|---------------|
| SM-01 | Create state machine | Unit | `mvn test -Dtest="StateMachineServiceTest"` | Will create |
| SM-02 | Define state specs | Unit | `mvn test -Dtest="StateMachineServiceTest"` | Will create |
| SM-03 | Define transition specs | Unit | `mvn test -Dtest="StateMachineServiceTest"` | Will create |
| SM-04 | List state machines | Unit | `mvn test -Dtest="StateMachineControllerTest"` | Will create |
| SM-05 | Get SM by ID | Unit | `mvn test -Dtest="StateMachineControllerTest"` | Will create |
| SM-06 | Update SM config | Unit | `mvn test -Dtest="StateMachineServiceTest"` | Will create |
| TRAN-01 | Trigger transition | Unit | `mvn test -Dtest="StateTransitionServiceTest"` | Will create |
| TRAN-02 | Validate transition | Unit | `mvn test -Dtest="StateTransitionServiceTest"` | Will create |
| TRAN-03 | Reject invalid | Unit | `mvn test -Dtest="StateTransitionServiceTest"` | Will create |
| TRAN-04 | Create history | Unit | `mvn test -Dtest="StateHistoryServiceTest"` | Will create |
| HIST-01 | View history | Unit | `mvn test -Dtest="StateHistoryServiceTest"` | Will create |
| HIST-02 | History details | Unit | `mvn test -Dtest="StateHistoryServiceTest"` | Will create |
| HIST-03 | Current state snapshot | Unit | `mvn test -Dtest="StateHistoryServiceTest"` | Will create |

### Sampling Rate
- **Per task commit:** `mvn test -Dtest="*StateMachine*Test" -q`
- **Per wave merge:** `mvn test -q`
- **Phase gate:** Full suite green before `/gsd-verify-work`

### Wave 0 Gaps
- [ ] `src/test/java/com/bedomain/service/StateMachineServiceTest.java` — covers SM-01 to SM-06
- [ ] `src/test/java/com/bedomain/controller/StateMachineControllerTest.java` — covers SM-04, SM-05
- [ ] `src/test/java/com/bedomain/service/StateTransitionServiceTest.java` — covers TRAN-01 to TRAN-04
- [ ] `src/test/java/com/bedomain/service/StateHistoryServiceTest.java` — covers HIST-01 to HIST-03
- [ ] `src/test/java/com/bedomain/controller/EntityStateControllerTest.java` — covers TRAN-01, HIST-01

---

## Sources

### Primary (HIGH confidence)
- Spring State Machine Reference Documentation 4.0.x - https://docs.spring.io/spring-statemachine/docs/current/reference/
- Spring State Machine GitHub - https://github.com/spring-projects/spring-statemachine
- Baeldung: Guide to Spring State Machine - https://www.baeldung.com/spring-state-machine

### Secondary (MEDIUM confidence)
- Stack Overflow: Spring State Machine with JPA - https://stackoverflow.com/questions/69324247
- Ibrahim Gündüz: Spring StateMachine Explained - https://ibrahimgunduz34.medium.com/spring-statemachine-explained-managing-complex-workflows-with-ease-ccec9363e6ff
- Medium: Managing States with Spring State Machine - https://medium.com/javarevisited/managing-states-with-spring-state-machine-d8c382d5e36c

### Tertiary (LOW confidence)
- Stack Overflow: State Machine Persistence Issues - https://stackoverflow.com/questions/78474084

---

## Metadata

**Confidence breakdown:**
- Standard Stack: HIGH - Official Spring project with active development, clear documentation
- Architecture: HIGH - DDD pattern follows Phase 1 conventions, SSM patterns well-established
- Pitfalls: MEDIUM - Some issues from community reports, some SSM version-specific

**Research date:** 2026-03-02
**Valid until:** 2026-09-02 (6 months - SSM is stable)
