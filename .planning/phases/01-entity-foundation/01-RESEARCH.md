# Phase 1: Entity Foundation - Research

**Researched:** 2026-03-01
**Domain:** Spring Boot 3.x REST API with JPA, Redis, Keycloak JWT Auth
**Confidence:** HIGH

## Summary

This phase implements a foundational entity management layer using SpringBoot 3.2.0 with Java 21. The architecture follows DDD principles with clear separation between controllers, services, repositories, and domain entities. Key integrations include MySQL for persistence, Redis for caching entity type lookups, and Keycloak JWT authentication via Spring Security OAuth2 Resource Server.

**Primary recommendation:** Use SpringBoot 3.2.x with Spring Data JPA, configure OAuth2 Resource Server with Keycloak issuer-uri, implement Redis caching with Spring Cache abstraction, and use `@EnableJpaAuditing` for audit fields.

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
- Architecture: DDD with SpringBoot 3.x + Java 21
- Single-module Maven project
- RESTful API with JSON request/response
- Resource naming: `/api/v1/entity-types`, `/api/v1/entity-types/{id}/properties`, `/api/v1/entities`
- JPA/Hibernate for ORM with MySQL 8.x
- UUID primary keys (server-side generation)
- Soft deletes (deleted flag)
- Audit fields: createdAt, createdBy, updatedAt, updatedBy
- Keycloak JWT validation via Spring Security OAuth2 Resource Server
- All endpoints require JWT except `/actuator/health`
- Redis cache-aside pattern with TTL expiration
- Spring Boot Actuator for health endpoints

### Claude's Discretion
- Exact Redis TTL value (default 1 hour)
- Global exception handler implementation details
- Pagination implementation specifics
- DTO vs entity mapping approach

### Deferred Ideas (OUT OF SCOPE)
- State machine configuration (Phase 2)
- State transitions and history (Phase 2)
- Kafka event publishing (Phase 3)
- GraphQL API (explicitly out of scope for v1)
- Multi-tenancy (out of scope)
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| ETYP-01 | Create entity type with name and description | Standard REST POST pattern with JPA entity save |
| ETYP-02 | List all entity types | Spring Data repositories with pagination support |
| ETYP-03 | Get entity type by ID | JPA findById with custom exception handling |
| ETYP-04 | Update entity type | JPA save with dirty checking, audit field updates |
| ETYP-05 | Delete entity type | Soft delete via deleted flag |
| PROP-01 | Add property specification to entity type | One-to-many relationship in JPA |
| PROP-02 | List property specifications for entity type | Cascade fetch or explicit query |
| PROP-03 | Update property specification | Entity update with parent cascade |
| PROP-04 | Delete property specification | Remove from collection |
| PROP-05 | Domain data types: STRING, NUMBER, DATE, BOOLEAN, TEXT | Enum with JPA @Enumerated |
| ENTY-01 | Create entity instance with type reference and attributes | JPA entity with JSON attributes (JPA 2.2+ JSON column) |
| ENTY-02 | List entity instances with filtering | Spring Data query methods with specification |
| ENTY-03 | Get entity instance by ID | findById with type eager fetch |
| ENTY-04 | Update entity instance attributes | Dirty checking on entity |
| ENTY-05 | Delete entity instance | Soft delete |
| ENTY-06 | Entity instance stores id (GUID), creation date, created by | UUID generation, audit fields via JPA Auditing |
| AUTH-01 | API validates JWT token from Keycloak | Spring Security OAuth2 Resource Server with issuer-uri |
| AUTH-02 | All endpoints require valid JWT except health | SecurityFilterChain with requestMatchers |
| AUTH-03 | User context extracted from JWT for audit | SecurityContextHolder, @CreatedBy integration |
| INFRA-01 | Application connects to MySQL | spring-boot-starter-data-jpa + mysql-connector-j |
| INFRA-02 | Application connects to Redis | spring-boot-starter-data-redis with RedisCacheManager |
| INFRA-04 | Application health endpoint | spring-boot-starter-actuator with /actuator/health |
</phase_requirements>

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Spring Boot | 3.2.0 | Application framework | Industry standard for Java microservices |
| Java | 21 | Language | Current LTS with virtual threads |
| Spring Data JPA | (via Spring Boot) | ORM abstraction | Eliminates boilerplate, standard JPA usage |
| Hibernate | 6.x (via Spring Boot) | JPA implementation | Most mature JPA provider |
| MySQL Connector | mysql-connector-j 8.x | MySQL driver | Official MySQL driver |
| Spring Security OAuth2 Resource Server | (via Spring Boot) | JWT validation | Official Spring Security JWT support |
| Spring Boot Actuator | (via Spring Boot) | Health/metrics | Standard health endpoint |
| Spring Data Redis | (via Spring Boot) | Redis integration | Standard Spring caching |
| Lombok | (via Spring Boot) | Boilerplate reduction | Standard in Spring projects |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| Jackson JSR310 | jackson-datatype-jsr310 | LocalDateTime serialization | Required for JSON/DB date handling |
| Spring Validation | spring-boot-starter-validation | Bean Validation | Input validation on DTOs |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| MySQL | PostgreSQL | MySQL specified in requirements |
| UUID v4 | UUID v7 | v7 is time-ordered but requires custom generator; v4 sufficient for v1 |
| Spring Security OAuth2 | Manual JWT validation with jjwt | OAuth2 Resource Server is standard, less maintenance |

## Architecture Patterns

### Recommended Project Structure
```
src/main/java/com/bedomain/
├── BedomainApplication.java
├── config/
│   ├── SecurityConfig.java
│   ├── RedisConfig.java
│   └── JpaAuditingConfig.java
├── controller/
│   ├── EntityTypeController.java
│   ├── PropertyController.java
│   └── EntityInstanceController.java
├── service/
│   ├── EntityTypeService.java
│   ├── PropertyService.java
│   └── EntityInstanceService.java
├── repository/
│   ├── EntityTypeRepository.java
│   ├── PropertyRepository.java
│   └── EntityInstanceRepository.java
├── domain/
│   ├── entity/
│   │   ├── EntityType.java
│   │   ├── Property.java
│   │   └── EntityInstance.java
│   ├── dto/
│   │   ├── EntityTypeRequest.java
│   │   ├── EntityTypeResponse.java
│   │   └── ...
│   └── enums/
│       └── DataType.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── BadRequestException.java
└── security/
    └── JwtAuthenticationExtractor.java
```

### Pattern 1: DDD Layered Architecture
**What:** Clear separation between controller (API), service (business logic), repository (data access), and domain (entities)
**When to use:** Standard enterprise Java applications
**Example:**
```java
@RestController
@RequestMapping("/api/v1/entity-types")
public class EntityTypeController {
    private final EntityTypeService service;
    
    @PostMapping
    public ResponseEntity<EntityTypeResponse> create(@Valid @RequestBody EntityTypeRequest request) {
        return ResponseEntity.ok(service.create(request));
    }
}

@Service
@Transactional
public class EntityTypeService {
    private final EntityTypeRepository repository;
    private final EntityTypeCacheService cacheService;
    
    public EntityType create(EntityTypeRequest request) {
        // Business logic here
        return repository.save(entity);
    }
}
```

### Pattern 2: JPA Auditing with Spring Data
**What:** Automatic population of createdAt, createdBy, updatedAt, updatedBy fields
**When to use:** Any entity requiring audit trails
**Example:**
```java
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {
    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
            .map(a -> a.getName());
    }
}

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable {
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    @CreatedBy
    @Column(updatable = false)
    private String createdBy;
    
    @LastModifiedBy
    private String updatedBy;
}
```

### Pattern 3: OAuth2 Resource Server with Keycloak
**What:** JWT token validation via Keycloak issuer-uri
**When to use:** Any API requiring Keycloak JWT authentication
**Example:**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(Customizer.withDefaults())
            );
        return http.build();
    }
}
```
```yaml
# application.yml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/bedomain
```

### Pattern 4: Redis Cache-Aside Pattern
**What:** Manual cache management with Redis for entity type lookups
**When to use:** Read-heavy entity type queries
**Example:**
```java
@Service
public class EntityTypeCacheService {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String CACHE_PREFIX = "entity-type:";
    private static final Duration TTL = Duration.ofHours(1);
    
    public Optional<EntityType> findById(UUID id) {
        String key = CACHE_PREFIX + id;
        EntityType cached = (EntityType) redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return Optional.of(cached);
        }
        return Optional.empty();
    }
    
    public void cache(EntityType entity) {
        redisTemplate.opsForValue().set(CACHE_PREFIX + entity.getId(), entity, TTL);
    }
    
    public void evict(UUID id) {
        redisTemplate.delete(CACHE_PREFIX + id);
    }
}
```

### Pattern 5: Global Exception Handler
**What:** Centralized error handling with consistent JSON responses
**When to use:** Any production REST API
**Example:**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ApiError(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                request.getRequestURI()
            ));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        // Handle validation errors
    }
}

public record ApiError(int status, String message, String path) {}
```

### Anti-Patterns to Avoid
- **Business logic in Controllers:** Controllers should only handle HTTP concerns; delegate all logic to services
- **Entities as API responses:** Use DTOs to decouple API contract from database schema
- **Caching without invalidation:** Always evict cache on updates/deletes
- **Exposing internal exceptions:** Never return raw stack traces to clients
- **Ignoring pagination:** List endpoints should always support pagination for production scale

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| JWT validation | Custom token parsing | Spring Security OAuth2 Resource Server | Handles JWKS, expiration, signature validation automatically |
| Database persistence | Raw JDBC | Spring Data JPA + Hibernate | Eliminates boilerplate, provides transaction management |
| Audit fields | Manual timestamp/user tracking | @EnableJpaAuditing | Automatic, integrated with Spring Security |
| Redis caching | Raw Redis commands | Spring Cache abstraction | Standard annotations, easier testing |
| Exception handling | Scattered try-catch | @RestControllerAdvice | Consistent error responses across API |
| Database connection pooling | Manual connection management | HikariCP (via Spring Boot) | Production-grade connection pooling |
| Input validation | Manual validation | Bean Validation + @Valid | Standard annotation-based validation |

**Key insight:** Spring Boot ecosystem provides production-grade solutions for all common concerns. Custom implementations add maintenance burden without benefits.

## Common Pitfalls

### Pitfall 1: UUID Generation Strategy Mismatch
**What goes wrong:** Using `GenerationType.AUTO` with MySQL generates TABLE strategy, causing issues with UUIDs
**Why it happens:** JPA default AUTO strategy doesn't always generate server-side UUIDs properly
**How to avoid:** Use `@GeneratedValue(generator = "UUID")` with `@GenericGenerator` or use database-generated UUIDs
**Warning signs:** `GenerationType.IDENTITY` fails with UUIDs on some databases; always specify strategy explicitly

### Pitfall 2: Missing @Transactional on Service Layer
**What goes wrong:** Lazy loading fails outside transaction boundaries, causing `LazyInitializationException`
**Why it happens:** JPA session closes after repository method returns
**How to avoid:** Always annotate service methods with `@Transactional`, use `fetch = FetchType.EAGER` or join queries when needed

### Pitfall 3: N+1 Query Problem
**What goes wrong:** Fetching entity with collection triggers separate query per item
**Why it happens:** Default lazy loading on one-to-many relationships
**How to avoid:** Use `@EntityGraph`, `JOIN FETCH` in JPQL, or `@BatchSize`

### Pitfall 4: Cache Stampede
**What goes wrong:** Multiple concurrent requests hit database when cache expires simultaneously
**Why it happens:** No cache locking mechanism
**How to avoid:** Implement cache-aside with distributed locking or use write-through for critical data

### Pitfall 5: Keycloak Unavailable at Startup
**What goes wrong:** Application fails to start if Keycloak is down, due to issuer-uri validation
**Why it happens:** Default OAuth2 Resource Server validates issuer at startup
**How to avoid:** Use `jwk-set-uri` for resilience, or set `spring.security.oauth2.resourceserver.jwt.jwk-set-uri` separately

### Pitfall 6: Circular Dependency in DTO Mapping
**What goes wrong:** Entity-to-DTO conversion fails with circular references
**Why it happens:** JPA entities have bidirectional relationships, Jackson tries to serialize infinitely
**How to avoid:** Use `@JsonIgnore`, separate DTOs from entities, use MapStruct

## Code Examples

### Entity Type with Audit Fields
```java
// Source: Research - Spring Data JPA patterns
@Entity
@Table(name = "entity_types")
public class EntityType extends Auditable {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Column(nullable = false)
    private boolean deleted = false;
    
    @OneToMany(mappedBy = "entityType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Property> properties = new ArrayList<>();
}
```

### Property Specification with Enum Data Type
```java
// Source: Research - JPA enum handling
@Entity
@Table(name = "properties")
public class Property {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DataType dataType;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_type_id")
    private EntityType entityType;
}

public enum DataType {
    STRING, NUMBER, DATE, BOOLEAN, TEXT
}
```

### Entity Instance with JSON Attributes
```java
// Source: Research - JPA 2.2+ JSON support
@Entity
@Table(name = "entity_instances")
public class EntityInstance extends Auditable {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_type_id", nullable = false)
    private EntityType entityType;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, Object> attributes;
    
    @Column(nullable = false)
    private boolean deleted = false;
}
```

### JWT User Extraction for Audit
```java
// Source: Research - Spring Security integration
@Service
public class AuditUserProvider implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
            .filter(Authentication::isAuthenticated)
            .map(auth -> {
                // Prefer preferred_username claim, fall back to sub
                if (auth.getPrincipal() instanceof Jwt jwt) {
                    return jwt.getClaimAsString("preferred_username");
                }
                return auth.getName();
            });
    }
}
```

### Redis Cache Configuration with TTL
```java
// Source: Research - Spring Data Redis
@Configuration
@EnableCaching
public class RedisCacheConfig {
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new Jackson2JsonRedisSerializer<>(Object.class)))
            .disableCachingNullValues();
        
        Map<String, RedisCacheConfiguration> cacheConfigs = Map.of(
            "entityTypes", defaultConfig.entryTtl(Duration.ofHours(1)),
            "entityTypeByName", defaultConfig.entryTtl(Duration.ofMinutes(30))
        );
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigs)
            .build();
    }
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Spring Security XML config | Java-based SecurityFilterChain | Spring Security 5.7+ / Spring Boot 3.x | Lambda DSL, more type-safe |
| `WebSecurityConfigurerAdapter` | Component-based SecurityFilterChain | Spring Security 5.7+ | Deprecated, replaced with functional style |
| GenerationType.AUTO | Explicit strategy per database | Hibernate 5.x+ | Predictable ID generation |
| `spring-boot-starter-security` | `spring-boot-starter-oauth2-resource-server` | Spring Boot 2.x+ | Dedicated resource server support |
| `@Transactional` on controllers | `@Transactional` on services | DDD best practices | Proper layer separation |

**Deprecated/outdated:**
- `WebSecurityConfigurerAdapter`: Deprecated in Spring Security 5.7, removed in 6.x
- `HttpSecurity.authorizeRequests()`: Replaced with `authorizeHttpRequests()`
- `AuthenticationPrincipal` extraction: Now integrated with `SecurityContextHolder`

## Open Questions

1. **Keycloak realm configuration details**
   - What we know: Requires issuer-uri matching Keycloak realm URL
   - What's unclear: Whether realm roles need custom JWT converter for @Secured annotations
   - Recommendation: Start with default scope-based authorities, add custom converter only if needed

2. **Redis cluster vs standalone**
   - What we know: Requirements mention "Redis HA Cluster"
   - What's unclear: Exact cluster configuration needed
   - Recommendation: Use Spring Data Redis ClusterConfiguration; start with standalone for dev

3. **Soft delete implementation approach**
   - What we know: CONTEXT.md specifies soft deletes with deleted flag
   - What's unclear: Whether to use Hibernate filters or manual where clauses
   - Recommendation: Add `deleted = false` to all repository queries; consider Hibernate @Where annotation

## Sources

### Primary (HIGH confidence)
- Spring Boot 3.2 Documentation - OAuth2 Resource Server
- Spring Data JPA Reference - Auditing
- Spring Security Reference - JWT Resource Server
- Keycloak 24+ Documentation - Spring Boot Integration

### Secondary (MEDIUM confidence)
- Dev.to: Keycloak Spring Boot OAuth2 Integration Guide
- Baeldung: Spring Boot Keycloak Tutorial
- Baeldung: Database Auditing with JPA

### Tertiary (LOW confidence)
- Various Medium tutorials on Spring Boot + Redis caching
- Community patterns for DDD in Spring Boot

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - All libraries from official Spring ecosystem with verified versions
- Architecture: HIGH - DDD patterns well-established, project structure matches industry standards
- Pitfalls: MEDIUM - Common issues verified across multiple sources

**Research date:** 2026-03-01
**Valid until:** 2026-04-01 (30 days for stable Spring ecosystem)
