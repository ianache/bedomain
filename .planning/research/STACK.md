# Stack Research

**Domain:** Business Entities Management Microservice
**Researched:** 2026-03-01
**Confidence:** HIGH

## Recommended Stack

### Core Framework

| Technology | Version | Purpose | Why Recommended |
|------------|---------|---------|-----------------|
| Spring Boot | 3.5.x (latest 3.5.9) | Application framework | Current stable version with Java 21 support, Jakarta EE 10, virtual threads, improved observability. Active community, enterprise adoption. |
| Java | 21 (LTS) | Language runtime | Required by project spec. Full virtual threads support, pattern matching, records. LTS until 2031. |
| Jakarta EE | 10 | API namespace | Required by Spring Boot 3.x. Replaces javax.* with jakarta.* |

### Database Layer

| Technology | Version | Purpose | Why Recommended |
|------------|---------|---------|-----------------|
| Spring Data JPA | (via Spring Boot BOM) | ORM abstraction | Standard Spring Data with Hibernate as implementation. Type-safe queries via repositories. |
| Hibernate | 6.4.x (via Spring Boot) | JPA implementation | Jakarta EE 10 compatible. Improved performance, lazy loading enhancements. |
| MySQL Connector/J | 8.4.x | MySQL driver | Required for MySQL 4.7+. Ensure driver version matches MySQL server version for best compatibility. |
| MySQL | 4.7 (8.0.x) | Primary database | Per architecture spec. Use 8.0 for better JSON support, window functions, CTEs. |
| Flyway | (via Spring Boot) | Database migrations | Declarative schema versioning. Integrates autoically with Spring Boot. Preferred over Liquibase for simple migrations. |

### Caching

| Technology | Version | Purpose | Why Recommended |
|------------|---------|---------|-----------------|
| Spring Data Redis | (via Spring Boot BOM) | Redis abstraction | Standard Spring Data with RedisTemplate. Cluster support, serialization options. |
| Lettuce | 6.3.x | Redis client | Default for Spring Data Redis. Supports Redis Cluster, Sentinel, reactive operations. |
| Redis | 7.x | Caching layer | Per architecture spec. Use 7.x for newer data structures, ACLs, improved performance. |

### Messaging

| Technology | Version | Purpose | Why Recommended |
|------------|---------|---------|-----------------|
| Spring Kafka | (via Spring Boot BOM) | Kafka abstraction | Standard Spring abstraction over Kafka. Auto-configuration, listener containers. |
| Apache Kafka | 3.7.x | Event streaming | Per architecture spec. 3.7 is latest stable with KRaft mode, improved scalability. |

### Security

| Technology | Version | Purpose | Why Recommended |
|------------|---------|---------|-----------------|
| Spring Security OAuth2 Resource Server | 6.4.x | JWT validation | Spring Security 6.4 (shipped with Spring Boot 3.5.x). Built-in JWT decoder, JWKSet support. |
| Keycloak Spring Boot Adapter | 25.0.x | Keycloak integration | Current stable. Quarkus-based runtime, improved performance. Use Keycloak 26.x for production. |
| Keycloak | 26.x | Identity Provider | Latest stable. OAuth2/OIDC, SSO, user federation, fine-grained authorization. |

### Supporting Libraries

| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| springdoc-openapi | 2.5.x | OpenAPI/Swagger docs | API-first development, generate OpenAPI specs from annotations. NOT 3.0.0-M1 (requires Spring Boot 4). |
| MapStruct | 1.6.x | DTO mapping | When converting between entities and DTOs. Compile-time safe, faster than reflection-based mappers. |
| Lombok | 1.18.x | Boilerplate reduction | Reduces getters/setters/constructors. Optional but improves readability. |
| Resilience4j | 2.2.x | Circuit breaker/rate limiter | For resilience patterns. Preferred over Netflix Hystrix (deprecated). |
| Micrometer | (via Spring Boot) | Metrics | Built-in observability. Prometheus, Grafana integration. |
| OpenTelemetry | 1.3x | Distributed tracing | OTLP export, vendor-neutral. Spring Boot 3.4+ has native support. |

### Build Tools

| Tool | Purpose | Notes |
|------|---------|-------|
| Maven 3.9.x | Build tool | Spring Boot 3.5 supports Maven 3.6.3+. Use 3.9.x for latest features. |
| Spring Boot Maven Plugin | Packaging | Creates executable JARs, supports layer extraction for containers. |

## Installation

```xml
<!-- pom.xml core dependencies -->
<properties>
    <java.version>21</java.version>
    <spring-boot.version>3.5.5</spring-boot.version>
    <keycloak.version>25.0.3</keycloak.version>
</properties>

<dependencies>
    <!-- Core -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- Database -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-mysql</artifactId>
    </dependency>
    
    <!-- Cache -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-cache</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    
    <!-- Messaging -->
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>
    
    <!-- Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>
    <dependency>
        <groupId>org.keycloak</groupId>
        <artifactId>keycloak-spring-boot-starter</artifactId>
        <version>${keycloak.version}</version>
    </dependency>
    
    <!-- Observability -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-prometheus</artifactId>
    </dependency>
    
    <!-- API Docs -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.5.0</version>
    </dependency>
    
    <!-- Utilities -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>1.6.3</version>
    </dependency>
    
    <!-- Resilience -->
    <dependency>
        <groupId>io.github.resilience4j</groupId>
        <artifactId>resilience4j-spring-boot3</artifactId>
        <version>2.2.0</version>
    </dependency>
    
    <!-- Test -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>testcontainers</artifactId>
        <version>1.20.0</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>1.20.0</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

## Alternatives Considered

| Recommended | Alternative | When to Use Alternative |
|-------------|-------------|-------------------------|
| Spring Boot 3.5.x | Spring Boot 4.0.x (when stable) | Wait until Spring Boot 4.0 has mature ecosystem (2026). Breaking changes from Spring Security 7. |
| Flyway | Liquibase | Use Liquibase if you need complex rollbacks, cross-database support, or team with SQL Server background. |
| Lettuce | Jedis | Jedis is simpler but less features. Use for simple use cases or if team familiarity. |
| Resilience4j | Spring Retry | Use Spring Retry for simple retry logic without circuit breaker. |
| MySQL 8.0 | PostgreSQL | Consider PostgreSQL if complex JSON operations, GIS, or advanced SQL features needed. |
| Keycloak | Auth0/Okta | Use SaaS providers if you don't want to operate your own IdP. |

## What NOT to Use

| Avoid | Why | Use Instead |
|-------|-----|-------------|
| Spring Boot 2.x | End of life, no Jakarta EE support | Spring Boot 3.5.x |
| Java 17 or below | Project requires Java 21 per spec | Java 21 |
| Netflix Hystrix | Deprecated, in maintenance mode | Resilience4j |
| Spring Cloud Netflix | In maintenance mode | Spring Cloud Gateway, Resilience4j standalone |
| javax.* packages | Replaced by jakarta.* in Jakarta EE 10 | jakarta.* |
| spring-cloud-starter-openfeign | Netflix components deprecated | Spring Cloud Gateway + RestClient, or WebClient |
| Swagger 2 (springfox) | No Spring Boot 3.x support | springdoc-openapi 2.5.x |
| Liquibase (if simple needs) | More complex than needed | Flyway (simpler, lower overhead) |
| Spring Security 5.x | Not compatible with Spring Boot 3.x | Spring Security 6.x (via Spring Boot 3.5) |

## Stack Patterns by Variant

**If you need event sourcing:**
- Add Axon Framework (4.x or 5.x for Axon Server)
- Use Spring Data JDBC instead of JPA for event sourcing
- Because: JPA's session management conflicts with event sourcing patterns

**If you need multi-tenancy:**
- Add hibernate-orm-multi-tenancy
- Use schema-per-tenant or discriminator-based approaches
- Because: Keycloak supports tenant isolation via realms

**If you need GraphQL:**
- Add spring-boot-starter-graphql
- Consider springdoc-openapi-to-graphQL as migration path
- Because: REST is out of scope per PROJECT.md

## Version Compatibility

| Package | Compatible With | Notes |
|---------|-----------------|-------|
| Spring Boot 3.5.x | Java 17-25 | Java 21 is target |
| Spring Security 6.4.x | Spring Boot 3.5.x | OAuth2 resource server built-in |
| Keycloak 25.x | Spring Boot 3.x | Use keycloak-spring-boot-starter |
| MySQL Connector 8.4.x | MySQL 8.0.x - 8.4.x | Matches MySQL 8.x |
| Kafka 3.7.x | Spring Kafka 3.2.x | Use Spring Boot BOM for version |
| Redis 7.x | Lettuce 6.3.x | Spring Data Redis manages version |

## Sources

- Spring Boot 3.5.9 Release — https://spring.io/blog/2025/12/18/spring-boot-3-5-9-available-now
- Spring Boot System Requirements — https://docs.spring.io/spring-boot/system-requirements.html
- Spring Security 6.4 Blog — https://spring.io/blog/2024/11/24/bootiful-34-security/
- Keycloak 26.x Release Notes — https://www.keycloak.org/docs/latest/release_notes/index.html
- Keycloak Spring Boot Starter 25.0.3 — https://central.sonatype.com/artifact/org.keycloak/keycloak-spring-boot-starter-parent
- Springdoc OpenAPI Starter — https://github.com/springdoc/springdoc-openapi (NOT 3.0.0-M1)
- CodeSearch: Spring Boot 3.5 Keycloak Redis microservice examples — Multiple 2025 articles confirm this stack

---

*Stack research for: bedomain - Business Entities Management Domain Service*
*Researched: 2026-03-01*
