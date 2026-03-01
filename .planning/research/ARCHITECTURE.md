# Architecture Research

**Domain:** Business Entities Management Microservice
**Researched:** 2026-03-01
**Confidence:** HIGH

## Standard Architecture

### System Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                     API Layer (Controllers)                         │
├─────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐    │
│  │ EntityTypeCtrl  │  │ EntityCtrl      │  │ StateMachineCtrl│    │
│  └────────┬────────┘  └────────┬────────┘  └────────┬────────┘    │
└───────────┼───────────────────┼───────────────────┼───────────────┘
            │                   │                   │
┌───────────┼───────────────────┼───────────────────┼───────────────┐
│           ▼                   ▼                   ▼               │
│                     Service Layer (Business Logic)                 │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐    │
│  │ EntityTypeSvc   │  │ EntitySvc       │  │ StateMachineSvc │    │
│  │ - CRUD ops      │  │ - CRUD ops      │  │ - Transitions   │    │
│  │ - Validation    │  │ - State changes │  │ - Rules engine  │    │
│  └────────┬────────┘  └────────┬────────┘  └────────┬────────┘    │
│           │                    │                    │              │
├───────────┼────────────────────┼────────────────────┼──────────────┤
│           ▼                    ▼                    ▼               │
│                     Domain Layer (Core Business)                    │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐    │
│  │ EntityType      │  │ BusinessEntity  │  │ StateMachine    │    │
│  │ - Definition    │  │ - Instance      │  │ - States       │    │
│  │ - Properties    │  │ - Current state│  │ - Transitions  │    │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘    │
│           │                    │                    │              │
│           └────────────────────┼────────────────────┘              │
│                                ▼                                    │
│                    Event Publisher (Domain Events)                   │
└─────────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    Infrastructure Layer                             │
├─────────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐                │
│  │ Repository │  │   Kafka     │  │   Redis    │                │
│  │   (JPA)    │  │  Publisher  │  │    Cache   │                │
│  └──────┬──────┘  └──────┬──────┘  └─────────────┘                │
│         │                 │                                         │
│         ▼                 ▼                                         │
│  ┌─────────────┐  ┌─────────────┐                                  │
│  │   MySQL     │  │   Kafka     │                                  │
│  │  Database   │  │   Topics    │                                  │
│  └─────────────┘  └─────────────┘                                  │
└─────────────────────────────────────────────────────────────────────┘
```

### Component Responsibilities

| Component | Responsibility | Typical Implementation |
|-----------|----------------|------------------------|
| Controller Layer | HTTP request handling, input validation, response formatting | Spring `@RestController`, `@RequestMapping` |
| Service Layer | Business logic orchestration, transaction management, state transition coordination | Spring `@Service`, `@Transactional` |
| Domain Layer | Core business rules, entity definitions, state machine logic | Domain entities with `@Entity`, value objects |
| Repository Layer | Data persistence abstraction, query execution | Spring Data JPA `JpaRepository` |
| Event Publisher | Domain event publishing to Kafka | Spring `@EventListener`, KafkaTemplate |
| Cache Layer | Hot data caching, session data | Spring Cache with Redis |
| Security Layer | JWT validation, authorization | Spring Security with Keycloak |

## Recommended Project Structure

```
src/main/java/com/bedomain/
├── BedomainApplication.java
├── config/
│   ├── SecurityConfig.java
│   ├── KafkaConfig.java
│   ├── RedisConfig.java
│   └── CacheConfig.java
├── controller/
│   ├── entitytype/
│   │   └── EntityTypeController.java
│   ├── entity/
│   │   └── BusinessEntityController.java
│   ├── statemachine/
│   │   └── StateMachineController.java
│   └── event/
│       └── BusinessEventController.java
├── service/
│   ├── EntityTypeService.java
│   ├── BusinessEntityService.java
│   ├── StateMachineService.java
│   └── BusinessEventService.java
├── domain/
│   ├── model/
│   │   ├── EntityType.java
│   │   ├── BusinessEntity.java
│   │   ├── EntityState.java
│   │   └── StateTransition.java
│   ├── valueobject/
│   │   ├── PropertyDefinition.java
│   │   └── TransitionRule.java
│   └── event/
│       ├── StateChangedEvent.java
│       └── EntityCreatedEvent.java
├── repository/
│   ├── EntityTypeRepository.java
│   ├── BusinessEntityRepository.java
│   └── StateHistoryRepository.java
├── dto/
│   ├── EntityTypeDTO.java
│   ├── BusinessEntityDTO.java
│   └── StateTransitionRequest.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── InvalidTransitionException.java
│   └── EntityNotFoundException.java
├── security/
│   ├── JwtAuthenticationFilter.java
│   └── SecurityUtils.java
└── event/
    ├── EventPublisher.java
    └── BusinessEventListener.java
```

### Structure Rationale

- **controller/:** REST endpoints grouped by domain area. Single responsibility per controller.
- **service/:** Business logic orchestration. No framework annotations on domain objects.
- **domain/model/:** Core business entities with behavior. JPA mappings here.
- **domain/valueobject/:** Immutable types representing business concepts without identity.
- **domain/event/:** Domain events that are published for state changes.
- **repository/:** Data access abstractions. Spring Data JPA interfaces.
- **dto/:** Data transfer objects for API requests/responses. Decoupled from persistence.
- **exception/:** Centralized error handling with `@ControllerAdvice`.
- **security/:** JWT and Keycloak integration components.
- **event/:** Kafka event publishing infrastructure.

## Architectural Patterns

### Pattern 1: Layered Architecture (Standard Spring)

**What:** Traditional controller → service → repository layering with clear dependency direction.
**When to use:** Default choice for most microservices. Simple, well-understood.
**Trade-offs:**
- Pros: Clear separation, easy to test, familiar pattern
- Cons: Can lead to anemic domain if service becomes a "transaction script"

**Example:**
```java
@RestController
@RequestMapping("/api/entity-types")
public class EntityTypeController {
    private final EntityTypeService service;
    
    @PostMapping
    public ResponseEntity<EntityTypeDTO> create(@Valid @RequestBody EntityTypeDTO dto) {
        return ResponseEntity.created(URI.create("/api/entity-types/" + service.create(dto)))
            .build();
    }
}

@Service
@Transactional
public class EntityTypeService {
    private final EntityTypeRepository repository;
    
    public UUID create(EntityTypeDTO dto) {
        var entity = new EntityType(dto.name(), dto.description());
        return repository.save(entity).getId();
    }
}
```

### Pattern 2: Domain-Driven Design (DDD) with Aggregates

**What:** Business entities organized as aggregates with clear boundaries. One entity is the aggregate root.
**When to use:** Complex business rules, entity lifecycle management, state machines.
**Trade-offs:**
- Pros: Encapsulates business logic, clear boundaries, supports complex state transitions
- Cons: More upfront design, requires DDD understanding

**Example:**
```java
@Entity
public class BusinessEntity extends AggregateRoot {
    @Id
    private UUID id;
    
    @Enumerated(EnumType.STRING)
    private EntityState currentState;
    
    @OneToMany(cascade = CascadeType.ALL)
    private List<StateHistory> history = new ArrayList<>();
    
    public void transitionTo(EntityState newState, TransitionRule rule) {
        if (!rule.isValid(currentState, newState)) {
            throw new InvalidTransitionException(currentState, newState);
        }
        this.currentState = newState;
        this.history.add(new StateHistory(currentState, newState, Instant.now()));
        addDomainEvent(new StateChangedEvent(this.id, currentState, newState));
    }
}
```

### Pattern 3: Event-Driven Architecture

**What:** Domain events drive communication. State changes publish events to Kafka for downstream consumption.
**When to use:** Microservices integration, audit requirements, eventual consistency.
**Trade-offs:**
- Pros: Loose coupling, audit trail, extensibility
- Cons: Eventual consistency, complexity in handling failures

**Example:**
```java
@Service
public class EventPublisher {
    private final KafkaTemplate<String, BusinessEvent> kafkaTemplate;
    
    @EventListener
    public void handleStateChanged(StateChangedEvent event) {
        kafkaTemplate.send("topic.businessevents", event.getEntityId().toString(), 
            new BusinessEvent(event.getType(), event.getPayload(), event.getTimestamp()));
    }
}
```

## Data Flow

### Request Flow: Create Entity with State Transition

```
[HTTP POST /api/entities]
        │
        ▼
[EntityController.create()]
        │ DTO: {typeId, properties, initialState}
        ▼
[BusinessEntityService.createEntity()]
        │
        ├──► [Validate EntityType exists]
        │
        ├──► [Create BusinessEntity aggregate]
        │         │
        │         ▼
        │    [EntityRepository.save()]
        │         │
        │         ▼
        │    [BusinessEntity.transitionTo(initialState)]
        │         │
        │         ├──► [StateMachineService.validateTransition()]
        │         │
        │         └──► [Add StateChangedEvent to domainEvents]
        │
        ├──► [Save again with history]
        │
        └──► [EventPublisher.publishEvents()]
                  │
                  ▼
            [Kafka: topic.businessevents]
```

### State Transition Flow

```
[HTTP POST /api/entities/{id}/transitions]
        │
        ▼
[EntityController.transition()]
        │ Request: {targetState, triggeredBy, metadata}
        ▼
[BusinessEntityService.transition()]
        │
        ├──► [Load BusinessEntity from repository]
        │
        ├──► [StateMachineService.getAllowedTransitions(currentState)]
        │
        ├──► [Validate transition is allowed]
        │
        ├──► [entity.transitionTo(targetState)]
        │         │
        │         └──► [Creates StateChangedEvent]
        │
        ├──► [repository.save(entity)]
        │         │
        │         └──► [Persists state + history]
        │
        └──► [EventPublisher.publish()]
                  │
                  ▼
            [Kafka: topic.businessevents]
                  │
                  ▼
            [topic.errorslog] (on failure)
```

### Caching Flow

```
[GET /api/entities/{id}]
        │
        ▼
[Controller]
        │
        ▼
[Cache lookup (Redis)]
        │ HIT → return cached DTO
        │ MISS → proceed to service
        ▼
[Service → Repository → DB]
        │
        ▼
[Cache put]
        │
        ▼
[Return DTO]
```

### Key Data Flows

1. **Entity CRUD:** Controller validates → Service orchestrates → Repository persists → Response flows back
2. **State Transitions:** Validate rule → Update entity → Record history → Publish event
3. **Event Publishing:** Domain event triggered → EventListener → KafkaTemplate → Kafka topic
4. **Cache Invalidation:** On entity update → Cache evict → Next read fetches fresh data

## Scaling Considerations

| Scale | Architecture Adjustments |
|-------|--------------------------|
| 0-1k users | Single instance, basic caching, no read replicas |
| 1k-100k users | Redis cache layer, connection pooling, read replicas for MySQL |
| 100k+ users | Horizontal scaling, Kafka partition strategy, consider CQRS |

### Scaling Priorities

1. **First bottleneck: Database writes** — Optimize with connection pooling, batch inserts
2. **Second bottleneck: Kafka throughput** — Partition by entity ID, batch producing
3. **Third bottleneck: Cache hit rate** — Analyze access patterns, warm cache on startup

## Anti-Patterns

### Anti-Pattern 1: Anemic Domain Model

**What people do:** Put all logic in Service layer, entities are just data holders with getters/setters.
**Why it's wrong:** Violates encapsulation, hard to enforce business rules, leads to service bloat.
**Do this instead:** Put behavior in domain entities. Service orchestrates but delegates to entities.

### Anti-Pattern 2: Exposing JPA Entities in API

**What people do:** Return JPA `@Entity` objects directly as controller responses.
**Why it's wrong:** Couples API to persistence, exposes internal structure, can leak data.
**Do this instead:** Use DTOs for all API input/output. Map in service layer.

### Anti-Pattern 3: Synchronous Kafka Publishing

**What people do:** Call `kafkaTemplate.send()` synchronously in the request thread.
**Why it's wrong:** Couples latency to network, blocks HTTP response, no retry handling.
**Do this instead:** Use `@Async` with `CompletableFuture` or the Transactional Outbox pattern.

## Integration Points

### External Services

| Service | Integration Pattern | Notes |
|---------|---------------------|-------|
| Keycloak | OAuth2 Resource Server with JWT | Validates Bearer tokens, extracts roles |
| Kafka | Async event publishing | topic.businessevents, topic.errorslog |
| Redis | Spring Cache abstraction | Cache entity metadata, state configs |

### Internal Boundaries

| Boundary | Communication | Notes |
|----------|---------------|-------|
| Controller → Service | Direct method call | Synchronous, transactional |
| Service → Repository | Spring Data interfaces | Synchronous |
| Domain → Event Publisher | Spring Events | Synchronous within transaction |
| Event Publisher → Kafka | Async fire-and-forget | Non-blocking |

## Build Order Recommendations

Based on dependencies between components:

1. **Foundation Layer** (build first)
   - `config/` - Security, Kafka, Redis configurations
   - `domain/model/` - Core entity definitions
   - `repository/` - Data access layer

2. **Core Business Layer** (depends on foundation)
   - `service/` - Business logic
   - `dto/` - Data transfer objects
   - `exception/` - Error handling

3. **API Layer** (depends on core)
   - `controller/` - REST endpoints
   - `security/` - JWT filters

4. **Integration Layer** (depends on core)
   - `event/` - Event publishing infrastructure

5. **Cross-Cutting**
   - Integration tests across layers

---

*Architecture research for: bedomain - Business Entities Management*
*Researched: 2026-03-01*
