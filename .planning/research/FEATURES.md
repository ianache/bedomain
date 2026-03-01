# Feature Research

**Domain:** Business Entities Management Microservice
**Researched:** 2026-03-01
**Confidence:** MEDIUM

## Feature Landscape

### Table Stakes (Users Expect These)

Features users assume exist. Missing these = product feels incomplete.

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| Entity Type CRUD API | Core requirement - users must be able to define and manage entity types (e.g., "Order", "Customer", "Invoice") | LOW | Standard REST endpoints; no complex logic |
| Entity Instance CRUD | Must create, read, update, delete entity instances of defined types | LOW | Standard REST patterns with entity type validation |
| Property Specifications | Entity types need dynamic properties (name, type, required, validation) | MEDIUM | Schema-like definition per entity type |
| State Machine Management | Define states and transitions for entity lifecycle (e.g., DRAFT → SUBMITTED → APPROVED → COMPLETED) | MEDIUM | Requires configurable state/transition model |
| State Transitions with Validation | Enforce valid transitions only; reject invalid state changes | MEDIUM | Core business logic enforcement |
| State History Tracking | Full audit trail of state changes (who, when, from→to) | MEDIUM | Required for compliance, debugging |
| REST API Authentication | Secure endpoints - JWT via Keycloak per architecture spec | LOW | Standard Spring Security integration |
| Basic Query/Filtering | Find entities by type, state, properties | LOW | Essential for usability |

### Differentiators (Competitive Advantage)

Features that set the product apart. Not required, but valuable.

| Feature | Value Proposition | Complexity | Notes |
|---------|-------------------|------------|-------|
| Declarative State Transition Rules | Define transitions via configuration, not code - business users can modify without deployment | HIGH | Key differentiator vs hardcoded state machines |
| Event-Driven Architecture (Kafka) | Publish business events for downstream services to react; enables reactive patterns | MEDIUM | Per architecture spec - major value add |
| Redis Caching Layer | High-throughput performance; cache entity types and frequent queries | MEDIUM | Architecture specifies Redis HA cluster |
| Business Event Type Management | Define custom event types; categorize and structure events | LOW | Enables downstream event processing |
| Property-Level Validation | Declarative validation rules per property (regex, range, custom) | MEDIUM | Goes beyond basic required/type checking |
| Entity Search with Advanced Filters | Complex queries across entity properties and states | MEDIUM | Essential for large datasets |

### Anti-Features (Commonly Requested, Often Problematic)

Features that seem good but create problems.

| Feature | Why Requested | Why Problematic | Alternative |
|---------|---------------|-----------------|-------------|
| GraphQL API | Flexibility in queries, nested fetches | Adds complexity; not needed for v1; steeper learning curve | REST API sufficient per PROJECT.md |
| Multi-tenancy Support | "Enterprise" feel, handle multiple orgs | Significant complexity; security concerns; not required for v1 | Single tenant for v1 |
| Complex Rules Engine | "What if we need complex validation?" | Scope creep; state machine transitions sufficient for v1 | Extend later if needed |
| Entity Versioning | Track all historical changes to entity data | Redundant with state history; adds storage overhead | State history provides audit trail |
| Built-in Web UI | "Users want a UI" | Out of scope per architecture; consumer services build UIs | API-first; consume from other services |
| Real-time WebSocket Updates | "Users want live state" | Complexity explosion; most use cases don't need it | Polling or event-driven consumption |

## Feature Dependencies

```
Entity Type Definition
    └──requires──> Property Specifications
                       └──requires──> State Machine Configuration

Entity Instance CRUD
    └──requires──> Entity Type Definition

State Transitions
    └──requires──> State Machine Configuration
    └──requires──> Entity Instance CRUD

State History Tracking
    └──requires──> State Transitions

Kafka Event Publishing
    └──requires──> State Transitions

Redis Caching
    └──enhances──> Entity Type Definition (caches types)
    └──enhances──> Entity Instance CRUD (caches instances)
```

### Dependency Notes

- **Entity Type Definition requires Property Specifications:** Entity types define what properties instances have
- **Entity Instance CRUD requires Entity Type Definition:** Can't create instances without knowing the type schema
- **State Transitions requires State Machine Configuration:** Must have defined states/transition rules before enforcing them
- **State Transitions requires Entity Instance CRUD:** Transitions modify entity instances
- **State History requires State Transitions:** Every transition creates a history entry
- **Kafka Event Publishing requires State Transitions:** Events are triggered by state changes
- **Redis Caching enhances Entity Type/Instance operations:** Performance optimization, not functional dependency

## MVP Definition

### Launch With (v1)

Minimum viable product — what's needed to validate the concept.

- [ ] Entity Type CRUD API — Core to the service
- [ ] Property Specifications — Dynamic entity schemas
- [ ] Entity Instance CRUD — Manage entities
- [ ] State Machine Configuration — Define states/transitions
- [ ] State Transitions with Validation — Enforce valid changes
- [ ] State History Tracking — Audit trail
- [ ] JWT Authentication via Keycloak — Security requirement
- [ ] Kafka Event Publishing — Architecture requirement

### Add After Validation (v1.x)

Features to add once core is working.

- [ ] Property-Level Validation — Deeper validation rules
- [ ] Advanced Search/Filtering — Complex queries
- [ ] Redis Caching Layer — Performance optimization

### Future Consideration (v2+)

Features to defer until product-market fit is established.

- [ ] Custom event type management
- [ ] Declarative transition rules editor
- [ ] Batch operations
- [ ] Import/export capabilities

## Feature Prioritization Matrix

| Feature | User Value | Implementation Cost | Priority |
|---------|------------|---------------------|----------|
| Entity Type CRUD | HIGH | LOW | P1 |
| Entity Instance CRUD | HIGH | LOW | P1 |
| Property Specifications | HIGH | MEDIUM | P1 |
| State Machine Configuration | HIGH | MEDIUM | P1 |
| State Transitions | HIGH | MEDIUM | P1 |
| State History Tracking | HIGH | MEDIUM | P1 |
| JWT Authentication | HIGH | LOW | P1 |
| Kafka Event Publishing | HIGH | MEDIUM | P1 |
| Redis Caching | MEDIUM | MEDIUM | P2 |
| Advanced Search | MEDIUM | MEDIUM | P2 |
| Property-Level Validation | MEDIUM | MEDIUM | P2 |

**Priority key:**
- P1: Must have for launch
- P2: Should have, add when possible
- P3: Nice to have, future consideration

## Competitor Feature Analysis

| Feature | Athennian (Legal) | Newton (Legal) | Adyen LEM API | Our Approach |
|---------|-------------------|----------------|---------------|--------------|
| Entity Type Definition | Yes (legal entities) | Yes | Yes (legal entities) | Yes - generic business entities |
| Property Specifications | Custom fields | Custom fields | Limited | Yes - dynamic schema |
| State Machine | Workflows | Compliance workflows | Limited | Yes - configurable |
| Audit History | Yes | Yes | Webhooks only | Yes - full history |
| Event-driven | Webhooks | API + webhooks | Webhooks | Kafka events |
| Multi-tenant | Yes | Yes | Per platform | Single tenant v1 |

## Sources

- Athennian product documentation (https://www.athennian.com/why-athennian)
- Newton Legal Entity Management (https://get-newton.com/features/)
- Adyen Legal Entity Management API (https://docs.adyen.com/api-explorer/legalentity/3/overview)
- Modern Treasury Legal Entity API (https://docs.moderntreasury.com/platform/reference/legal-entity-webhooks)
- Spring Statemachine documentation (https://docs.spring.io/spring-statemachine/docs/current/reference/)
- Microsoft REST API Design Best Practices (https://learn.microsoft.com/en-us/azure/architecture/best-practices/api-design)
- Industry analysis: entity management platforms (legal, corporate) vs generic business entity management

---
*Feature research for: Business Entities Management Microservice*
*Researched: 2026-03-01*
