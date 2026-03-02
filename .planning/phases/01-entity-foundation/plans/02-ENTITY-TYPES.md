# Plan: Entity Type & Property Management API

**Phase:** 1 - Entity Foundation
**Goal:** Implement CRUD APIs for entity types and property specifications

## Requirements Covered

- ETYP-01: User can create entity type with name and description
- ETYP-02: User can list all entity types
- ETYP-03: User can get entity type by ID
- ETYP-04: User can update entity type
- ETYP-05: User can delete entity type
- PROP-01: User can add property specification to entity type (name, description, data type)
- PROP-02: User can list property specifications for an entity type
- PROP-03: User can update property specification
- PROP-04: User can delete property specification
- PROP-05: Property specifications support domain data types (STRING, NUMBER, DATE, BOOLEAN, TEXT)

## Success Criteria

1. All CRUD endpoints for entity types work correctly
2. Property specifications linked to entity types
3. Proper validation and error handling
4. User context captured in audit fields

## Tasks

### 2.1 Entity Type Domain

- [ ] Create EntityType JPA entity (id, name, description, createdAt, createdBy, updatedAt, updatedBy)
- [ ] Create EntityTypeRepository interface
- [ ] Create EntityTypeService with CRUD operations
- [ ] Create EntityTypeController with REST endpoints
- [ ] Add caching for entity type lookups (Redis)

### 2.2 Property Specification Domain

- [ ] Create PropertySpec JPA entity (id, entityTypeId, name, description, dataType, createdAt, createdBy)
- [ ] Create DataType enum (STRING, NUMBER, DATE, BOOLEAN, TEXT)
- [ ] Create PropertySpecRepository interface
- [ ] Create PropertySpecService with CRUD operations
- [ ] Create PropertySpecController with REST endpoints

### 2.3 API Implementation

- [ ] POST /api/v1/entity-types - Create entity type
- [ ] GET /api/v1/entity-types - List all (with pagination)
- [ ] GET /api/v1/entity-types/{id} - Get by ID
- [ ] PUT /api/v1/entity-types/{id} - Update entity type
- [ ] DELETE /api/v1/entity-types/{id} - Delete entity type
- [ ] POST /api/v1/entity-types/{id}/properties - Add property
- [ ] GET /api/v1/entity-types/{id}/properties - List properties
- [ ] PUT /api/v1/entity-types/{id}/properties/{propId} - Update property
- [ ] DELETE /api/v1/entity-types/{id}/properties/{propId} - Delete property

### 2.4 Validation & Error Handling

- [ ] Add validation annotations (@NotBlank, @Size, etc.)
- [ ] Create GlobalExceptionHandler
- [ ] Handle EntityNotFoundException
- [ ] Handle validation errors with 400 Bad Request

### 2.5 Caching

- [ ] Cache entity types by ID
- [ ] Cache entity types by name
- [ ] Invalidate cache on entity type update/delete

## File Structure

```
src/main/java/com/bedomain/
├── entity/
│   ├── EntityType.java
│   └── PropertySpec.java
├── repository/
│   ├── EntityTypeRepository.java
│   └── PropertySpecRepository.java
├── service/
│   ├── EntityTypeService.java
│   └── PropertySpecService.java
├── controller/
│   ├── EntityTypeController.java
│   └── PropertySpecController.java
├── dto/
│   ├── CreateEntityTypeRequest.java
│   ├── UpdateEntityTypeRequest.java
│   ├── EntityTypeResponse.java
│   ├── CreatePropertySpecRequest.java
│   ├── UpdatePropertySpecRequest.java
│   └── PropertySpecResponse.java
├── exception/
│   ├── EntityNotFoundException.java
│   └── GlobalExceptionHandler.java
└── enums/
    └── DataType.java
```

## Entity Models

### EntityType
```java
@Entity
@Table(name = "entity_types")
public class EntityType {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    private String description;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;
    
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Column(name = "updated_by")
    private String updatedBy;
}
```

### PropertySpec
```java
@Entity
@Table(name = "property_specs")
public class PropertySpec {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "entity_type_id", nullable = false)
    private UUID entityTypeId;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DataType dataType;
    
    // audit fields...
}
```

### DataType Enum
```java
public enum DataType {
    STRING,
    NUMBER,
    DATE,
    BOOLEAN,
    TEXT
}
```

## API Examples

### Create Entity Type
```bash
POST /api/v1/entity-types
Authorization: Bearer <jwt>
Content-Type: application/json

{
  "name": "Customer",
  "description": "Represents a customer entity"
}
```

### List Entity Types
```bash
GET /api/v1/entity-types?page=0&size=20
```

### Add Property to Entity Type
```bash
POST /api/v1/entity-types/{id}/properties
Authorization: Bearer <jwt>
Content-Type: application/json

{
  "name": "email",
  "description": "Customer email address",
  "dataType": "STRING"
}
```

## Acceptance Criteria

- [ ] POST /api/v1/entity-types creates entity type, returns 201
- [ ] GET /api/v1/entity-types returns paginated list
- [ ] GET /api/v1/entity-types/{id} returns entity type
- [ ] PUT /api/v1/entity-types/{id} updates, returns 200
- [ ] DELETE /api/v1/entity-types/{id} deletes, returns 204
- [ ] POST /api/v1/entity-types/{id}/properties adds property
- [ ] GET /api/v1/entity-types/{id}/properties lists properties
- [ ] PUT /api/v1/entity-types/{id}/properties/{propId} updates property
- [ ] DELETE /api/v1/entity-types/{id}/properties/{propId} deletes property
- [ ] DataType enum supports all 5 types
- [ ] Invalid requests return 400 with error details
- [ ] Missing entity returns 404
