# Bedomain - Business Entity Domain Management Service

A Spring Boot microservice for managing business entities with dynamic property definitions, state machines, JWT authentication, and event-driven architecture.

## Overview

Bedomain provides a flexible framework for defining and managing business entities with:

- **Dynamic Entity Types** - Define custom entity types with arbitrary properties
- **Property Specifications** - Support for multiple data types (STRING, NUMBER, BOOLEAN, DATE, EMAIL, URL)
- **Entity Instances** - Create and manage instances of your entity types
- **State Machine** - Built-in state transition management (future)
- **Event Publishing** - Kafka-based event publishing for entity changes (future)

## Tech Stack

| Component | Technology |
|-----------|------------|
| **Framework** | Spring Boot 3.2.0 |
| **Language** | Java 21 |
| **Database** | MySQL 8.0 |
| **Cache** | Redis 7 |
| **Authentication** | Keycloak (OAuth2/OIDC) |
| **Build Tool** | Maven 3.9 |
| **Testing** | JUnit 5, H2 |
| **Code Coverage** | JaCoCo |
| **Container** | Docker, Docker Compose |

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      Bedomain Service                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │ Controllers │  │  Services   │  │    Repositories     │ │
│  └─────────────┘  └─────────────┘  └─────────────────────┘ │
│  ┌─────────────────────────────────────────────────────────┐│
│  │                    Domain Layer                         ││
│  │  EntityType │ Property │ EntityInstance │ DataTypes    ││
│  └─────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
         │                    │                    │
         ▼                    ▼                    ▼
   ┌──────────┐        ┌──────────┐        ┌──────────┐
   │   MySQL  │        │   Redis  │        │ Keycloak │
   └──────────┘        └──────────┘        └──────────┘
```

## Features

### Phase 1 - Entity Foundation (Complete)
- [x] Entity Type CRUD operations
- [x] Property Specification management
- [x] Entity Instance management
- [x] JWT Authentication via Keycloak
- [x] Redis caching
- [x] Soft delete with audit fields
- [x] Comprehensive unit tests (94% service coverage)

### Phase 2 - State Machine Core (Planned)
- State definitions per entity type
- Transition rules and validation
- State history tracking

### Phase 3 - Event Publishing (Planned)
- Kafka integration for entity events
- Event types: CREATED, UPDATED, DELETED, STATE_CHANGED

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+
- Docker & Docker Compose

### Local Development (Without Docker)

1. **Configure MySQL and Redis** (or use default):

```bash
# Default local configuration in application.yml
# MySQL: localhost:3306, database: bedomain
# Redis: localhost:6379
```

2. **Configure Keycloak**:
   - Start Keycloak locally or use Docker
   - Create realm `bedomain`
   - Configure client `bedomain-client`

3. **Build and Run**:

```bash
cd bedomain
mvn clean install
mvn spring-boot:run
```

### Docker Deployment

1. **Build and start all services**:

```bash
cd bedomain
docker compose up -d --build
```

2. **Verify services are running**:

```bash
docker compose ps
```

3. **Access the application**:
   - Bedomain API: http://localhost:8080
   - Keycloak: http://localhost:8180 (admin/admin)

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Spring profile | `docker` |
| `DB_HOST` | MySQL host | `localhost` |
| `DB_PORT` | MySQL port | `3306` |
| `DB_USERNAME` | MySQL user | `bedomain` |
| `DB_PASSWORD` | MySQL password | `bedomain123` |
| `REDIS_HOST` | Redis host | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `KEYCLOAK_ISSUER_URI` | Keycloak issuer | `http://keycloak:8080/realms/bedomain` |

## API Endpoints

### Entity Types

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/entity-types` | List all entity types |
| `GET` | `/api/entity-types/{id}` | Get entity type by ID |
| `POST` | `/api/entity-types` | Create entity type |
| `PUT` | `/api/entity-types/{id}` | Update entity type |
| `DELETE` | `/api/entity-types/{id}` | Soft delete entity type |

### Properties

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/entity-types/{entityTypeId}/properties` | List properties |
| `GET` | `/api/entity-types/{entityTypeId}/properties/{id}` | Get property |
| `POST` | `/api/entity-types/{entityTypeId}/properties` | Create property |
| `PUT` | `/api/entity-types/{entityTypeId}/properties/{id}` | Update property |
| `DELETE` | `/api/entity-types/{entityTypeId}/properties/{id}` | Delete property |

### Entity Instances

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/entity-types/{entityTypeId}/instances` | List instances |
| `GET` | `/api/entity-types/{entityTypeId}/instances/{id}` | Get instance |
| `POST` | `/api/entity-types/{entityTypeId}/instances` | Create instance |
| `PUT` | `/api/entity-types/{entityTypeId}/instances/{id}` | Update instance |
| `DELETE` | `/api/entity-types/{entityTypeId}/instances/{id}` | Soft delete instance |

## Request/Response Examples

### Create Entity Type

```bash
curl -X POST http://localhost:8080/api/entity-types \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Product",
    "description": "E-commerce product entity"
  }'
```

### Create Property

```bash
curl -X POST http://localhost:8080/api/entity-types/1/properties \
  -H "Content-Type: application/json" \
  -d '{
    "name": "price",
    "description": "Product price",
    "dataType": "NUMBER"
  }'
```

### Create Entity Instance

```bash
curl -X POST http://localhost:8080/api/entity-types/1/instances \
  -H "Content-Type: application/json" \
  -d '{
    "attributes": {
      "name": "iPhone 15 Pro",
      "price": 999.99,
      "inStock": true
    }
  }'
```

## Testing

Run unit tests with coverage report:

```bash
cd bedomain
mvn clean test
```

View coverage report:

```bash
# Open in browser
bedomain/target/site/jacoco/index.html
```

## Project Structure

```
bedomain/
├── src/
│   ├── main/
│   │   ├── java/com/bedomain/
│   │   │   ├── config/          # Spring configurations
│   │   │   ├── controller/       # REST controllers
│   │   │   ├── domain/           # Entities, DTOs, enums
│   │   │   ├── exception/       # Exception handling
│   │   │   ├── repository/      # JPA repositories
│   │   │   ├── security/        # JWT & auth
│   │   │   └── service/         # Business logic
│   │   └── resources/
│   │       └── application.yml  # Configuration
│   └── test/
│       └── java/com/bedomain/   # Unit tests
├── Dockerfile                    # Multi-stage build
├── docker-compose.yml            # Full stack deployment
└── pom.xml                       # Maven dependencies
```

## Configuration Files

| File | Purpose |
|------|---------|
| `application.yml` | Spring Boot configuration |
| `application-docker.yml` | Docker-specific overrides |
| `docker-compose.yml` | MySQL, Redis, Keycloak, App |
| `Dockerfile` | Container image build |

## Security

- All API endpoints require JWT authentication
- Token validation via Keycloak OAuth2 Resource Server
- Soft deletes preserve audit trail (createdBy, updatedBy, deletedAt)

## License

This project is private and proprietary.

## Contributing

For internal development, use the GSD (Get Shit Done) workflow:

```bash
# Plan a new phase
/gsd-plan-phase <phase-name>

# Execute planned tasks
/gsd-execute-phase <phase-name>

# Verify phase completion
/gsd-verify-phase <phase-name>
```
