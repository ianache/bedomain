# Plan: Infrastructure & Security Setup

**Phase:** 1 - Entity Foundation
**Goal:** Establish core infrastructure: MySQL, Redis, Health endpoints, Keycloak JWT authentication

## Requirements Covered

- INFRA-01: Application connects to MySQL database
- INFRA-02: Application connects to Redis for caching
- INFRA-04: Application health endpoint available
- AUTH-01: API validates JWT token from Keycloak
- AUTH-02: All endpoints require valid JWT (except health check)
- AUTH-03: User context extracted from JWT for audit trail

## Success Criteria

1. SpringBoot application starts and connects to MySQL
2. Redis connection configured and working
3. `/actuator/health` returns UP (public access)
4. All `/api/**` endpoints require valid JWT
5. User ID extracted from JWT `sub` claim for audit

## Tasks

### 1.1 Project Setup

- [ ] Create SpringBoot 3.x project with Maven
- [ ] Add dependencies: spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-data-redis, spring-boot-starter-oauth2-resource-server, spring-boot-starter-actuator, mysql-connector-j, lombok
- [ ] Configure Java 21

### 1.2 Database Configuration (INFRA-01)

- [ ] Configure MySQL DataSource in application.yml
- [ ] Create schema.sql or use Hibernate auto-ddl
- [ ] Add flyway/liquibase for migrations (optional, can use Hibernate for v1)
- [ ] Test database connection

### 1.3 Redis Configuration (INFRA-02)

- [ ] Configure Redis connection in application.yml
- [ ] Create RedisConfig bean with RedisTemplate
- [ ] Add caching configuration
- [ ] Test Redis connection

### 1.4 Health Endpoint (INFRA-04)

- [ ] Configure Spring Boot Actuator
- [ ] Enable `/actuator/health` with DB and Redis indicators
- [ ] Make health endpoint public

### 1.5 Keycloak JWT Authentication (AUTH-01, AUTH-02, AUTH-03)

- [ ] Configure OAuth2 Resource Server with Keycloak
- [ ] Set JWT decoder with Keycloak jwks-url
- [ ] Configure security filter chain: permit /actuator/**, require auth for /api/**
- [ ] Create JwtAuthenticationService to extract user context
- [ ] Add user context to SecurityContext

## File Structure

```
src/main/java/com/bedomain/
├── BedomainApplication.java
├── config/
│   ├── DatabaseConfig.java
│   ├── RedisConfig.java
│   ├── SecurityConfig.java
│   └── ActuatorConfig.java
└── security/
    └── JwtAuthenticationService.java

src/main/resources/
├── application.yml
└── schema.sql (or use Hibernate)
```

## Dependencies

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

## Configuration (application.yml)

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/bedomain?createDatabaseIfNotExist=true
    username: bedomain
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${KEYCLOAK_ISSUER_URI:http://localhost:8080/realms/bedomain}

management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always
      probes:
        enabled: true
```

## Acceptance Criteria

- [ ] Application starts without errors
- [ ] GET /actuator/health returns {"status":"UP"}
- [ ] GET /api/v1/entity-types returns 401 (no JWT)
- [ ] Request with valid JWT allows access to /api/**
- [ ] Redis ping returns PONG
- [ ] Database connection is UP in health check
