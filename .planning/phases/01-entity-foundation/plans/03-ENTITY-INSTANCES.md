# Plan: Entity Instance Management API

**Phase:** 1 - Entity Foundation
**Goal:** Implement CRUD APIs for entity instances with type reference and attributes

## Requirements Covered

- ENTY-01: User can create entity instance with type reference and initial attributes
- ENTY-02: User can list entity instances (with filtering by type and state)
- ENTY-03: User can get entity instance by ID
- ENTY-04: User can update entity instance attributes
- ENTY-05: User can delete entity instance
- ENTY-06: Entity instance stores id (GUID), creation date, created by

## Success Criteria

1. Entity instances created with reference to entity type
2. Attributes stored as JSON
3. Filtering by entity type works
4. Proper audit trail (createdBy, updatedBy from JWT)
5. Proper validation against property specifications

## Tasks

### 3.1 Entity Instance Domain

- [ ] Create EntityInstance JPA entity (id, entityTypeId, attributes as JSON, createdAt, createdBy, updatedAt, updatedBy)
- [ ] Create EntityInstanceRepository interface with custom queries
- [ ] Create EntityInstanceService with CRUD operations
- [ ] Create EntityInstanceController with REST endpoints

### 3.2 JSON Attribute Handling

- [ ] Configure Jackson ObjectMapper for JSON
- [ ] Store attributes as JSON column (or separate table for v1)
- [ ] Validate attributes against property specifications (type checking)

### 3.3 API Implementation

- [ ] POST /api/v1/entities - Create entity instance
- [ ] GET /api/v1/entities - List (with filtering by type, pagination)
- [ ] GET /api/v1/entities/{id} - Get by ID
- [ ] PUT /api/v1/entities/{id} - Update attributes
- [ ] DELETE /api/v1/entities/{id} - Delete entity instance

### 3.4 Filtering & Querying

- [ ] Add query parameter for entityTypeId filter
- [ ] Add query parameters for pagination (page, size, sort)
- [ ] Add basic search/filter capabilities

### 3.5 Validation

- [ ] Validate entityTypeId exists
- [ ] Validate attributes match property specifications (type validation)
- [ ] Handle validation errors

## File Structure

```
src/main/java/com/bedomain/
├── entity/
│   └── EntityInstance.java
├── repository/
│   └── EntityInstanceRepository.java
├── service/
│   └── EntityInstanceService.java
├── controller/
│   └── EntityInstanceController.java
└── dto/
    ├── CreateEntityInstanceRequest.java
    ├── UpdateEntityInstanceRequest.java
    └── EntityInstanceResponse.java
```

## Entity Model

### EntityInstance
```java
@Entity
@Table(name = "entity_instances")
public class EntityInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "entity_type_id", nullable = false)
    private UUID entityTypeId;
    
    @Column(columnDefinition = "JSON")
    private String attributes;
    
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

## API Examples

### Create Entity Instance
```bash
POST /api/v1/entities
Authorization: Bearer <jwt>
Content-Type: application/json

{
  "entityTypeId": "uuid-of-entity-type",
  "attributes": {
    "name": "John Doe",
    "email": "john@example.com",
    "age": 30,
    "active": true
  }
}
```

### List Entity Instances
```bash
GET /api/v1/entities?entityTypeId=uuid&page=0&size=20&sort=createdAt,desc
```

### Get Entity Instance
```bash
GET /api/v1/entities/{id}
```

### Update Entity Instance
```bash
PUT /api/v1/entities/{id}
Authorization: Bearer <jwt>
Content-Type: application/json

{
  "attributes": {
    "name": "John Doe Updated",
    "email": "john.updated@example.com",
    "age": 31
  }
}
```

### Delete Entity Instance
```bash
DELETE /api/v1/entities/{id}
```

## Acceptance Criteria

- [ ] POST /api/v1/entities creates instance, returns 201 with created instance
- [ ] GET /api/v1/entities returns paginated list
- [ ] GET /api/v1/entities?entityTypeId= filters by type
- [ ] GET /api/v1/entities/{id} returns instance
- [ ] PUT /api/v1/entities/{id} updates attributes, returns 200
- [ ] DELETE /api/v1/entities/{id} deletes, returns 204
- [ ] Entity instance includes id (UUID)
- [ ] Entity instance includes createdAt timestamp
- [ ] Entity instance includes createdBy from JWT
- [ ] Invalid entityTypeId returns 404
- [ ] Invalid attributes return 400
