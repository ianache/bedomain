---
phase: 01-entity-foundation
plan: 02b
type: execute
wave: 3
depends_on: ["01-02a"]
files_modified:
  - src/main/java/com/bedomain/service/EntityTypeService.java
  - src/main/java/com/bedomain/service/PropertyService.java
  - src/main/java/com/bedomain/controller/EntityTypeController.java
  - src/main/java/com/bedomain/controller/PropertyController.java
  - src/main/java/com/bedomain/exception/ResourceNotFoundException.java
  - src/main/java/com/bedomain/exception/GlobalExceptionHandler.java
autonomous: true
requirements:
  - ETYP-01
  - ETYP-02
  - ETYP-03
  - ETYP-04
  - ETYP-05
  - PROP-01
  - PROP-02
  - PROP-03
  - PROP-04
  - PROP-05

must_haves:
  truths:
    - "User can create entity type with name and description"
    - "User can list all entity types with pagination"
    - "User can get entity type by ID"
    - "User can update entity type"
    - "User can delete entity type (soft delete)"
    - "User can add property specification with data type"
    - "User can list, update, delete property specifications"
    - "Property specifications support STRING, NUMBER, DATE, BOOLEAN, TEXT data types"
  artifacts:
    - path: "src/main/java/com/bedomain/controller/EntityTypeController.java"
      provides: "Entity type REST endpoints"
      contains: "POST, GET, PUT, DELETE /api/v1/entity-types"
    - path: "src/main/java/com/bedomain/controller/PropertyController.java"
      provides: "Property specification REST endpoints"
      contains: "POST, GET, PUT, DELETE /api/v1/entity-types/{id}/properties"
    - path: "src/main/java/com/bedomain/service/EntityTypeService.java"
      provides: "Entity type business logic with Redis caching"
      contains: "create, findAll, findById, update, delete with cache management"
    - path: "src/main/java/com/bedomain/service/PropertyService.java"
      provides: "Property specification business logic"
      contains: "create, findByEntityTypeId, update, delete"
  key_links:
    - from: "EntityTypeController"
      to: "EntityTypeService"
      via: "dependency injection"
      pattern: "entityTypeService.create|update|delete|findById"
    - from: "EntityTypeService"
      to: "EntityTypeRepository"
      via: "JPA repository"
      pattern: "entityTypeRepository.save|findById|findAll"
---

<objective>
Implement services and controllers for entity types and property specifications. This provides the business logic and REST API layer for Phase 1.
</objective>

<context>
@.planning/phases/01-entity-foundation/01-CONTEXT.md
@.planning/phases/01-entity-foundation/01-RESEARCH.md

**Key constraints from CONTEXT.md:**
- Resource naming: /api/v1/entity-types, /api/v1/entity-types/{id}/properties
- Soft deletes (deleted flag)
- Redis cache-aside pattern for entity type lookups

**Depends on:**
- 01-02a: Domain entities, DTOs, and repositories must exist first
</context>

<tasks>

<task type="auto">
  <name>Task 1: Create EntityTypeService with caching</name>
  <files>src/main/java/com/bedomain/service/EntityTypeService.java</files>
  <action>
    Create EntityTypeService.java:
    - @Service @Transactional
    - EntityTypeRepository, PropertyRepository, CacheManager dependencies
    - create(request): validate name not blank/duplicate, save, cache result, return response
    - findAll(pageable): return findByDeletedFalse
    - findById(id): check cache first, then DB, throw ResourceNotFoundException
    - update(id, request): find, validate name uniqueness, update fields, evict cache, return response
    - delete(id): find, set deleted=true, save, evict cache
    - Cache key patterns: "entityType:id:{uuid}", "entityType:name:{name}"
  </action>
  <verify>
    <automated>grep -n "@Service\|@Transactional\|create\|findById\|update\|delete\|cache" src/main/java/com/bedomain/service/EntityTypeService.java</automated>
  </verify>
  <done>EntityTypeService with CRUD and Redis caching</done>
</task>

<task type="auto">
  <name>Task 2: Create PropertyService</name>
  <files>src/main/java/com/bedomain/service/PropertyService.java</files>
  <action>
    Create PropertyService.java:
    - @Service @Transactional
    - PropertyRepository, EntityTypeRepository dependencies
    - create(entityTypeId, request): find entityType, validate property name unique within type, save property, return response
    - findByEntityTypeId(entityTypeId): return findByEntityTypeId
    - update(entityTypeId, propertyId, request): find property, update fields, save, return response
    - delete(entityTypeId, propertyId): find and delete property
  </action>
  <verify>
    <automated>grep -n "@Service\|@Transactional\|create\|findByEntityTypeId\|update\|delete" src/main/java/com/bedomain/service/PropertyService.java</automated>
  </verify>
  <done>PropertyService with CRUD operations</done>
</task>

<task type="auto">
  <name>Task 3: Create exception classes</name>
  <files>src/main/java/com/bedomain/exception/ResourceNotFoundException.java, src/main/java/com/bedomain/exception/GlobalExceptionHandler.java</files>
  <action>
    Create ResourceNotFoundException.java:
    - @ResponseStatus(NOT_FOUND)
    - Constructor accepting message

    Create GlobalExceptionHandler.java:
    - @RestControllerAdvice
    - handleNotFound(ResourceNotFoundException): 404 with ApiError
    - handleValidation(MethodArgumentNotValidException): 400 with validation errors
    - handleGeneric(Exception): 500 with error details
  </action>
  <verify>
    <automated>grep -n "@RestControllerAdvice\|handleNotFound\|handleValidation" src/main/java/com/bedomain/exception/GlobalExceptionHandler.java</automated>
  </verify>
  <done>Exception handling configured</done>
</task>

<task type="auto">
  <name>Task 4: Create controllers</name>
  <files>src/main/java/com/bedomain/controller/EntityTypeController.java, src/main/java/com/bedomain/controller/PropertyController.java</files>
  <action>
    Create EntityTypeController.java:
    - @RestController @RequestMapping("/api/v1/entity-types")
    - EntityTypeService dependency
    - POST /: @Valid @RequestBody CreateEntityTypeRequest, return 201 with response
    - GET /: @RequestParam(defaultValue="0") int page, return Page<EntityTypeResponse>
    - GET /{id}: return EntityTypeResponse or 404
    - PUT /{id}: return EntityTypeResponse or 404
    - DELETE /{id}: return 204 or 404

    Create PropertyController.java:
    - @RestController @RequestMapping("/api/v1/entity-types/{entityTypeId}/properties")
    - PropertyService dependency
    - POST /: create property, return 201
    - GET /: list properties by entity type
    - PUT /{propertyId}: update property
    - DELETE /{propertyId}: delete property
  </action>
  <verify>
    <automated>grep -n "@RestController\|@RequestMapping\|POST\|GET\|PUT\|DELETE" src/main/java/com/bedomain/controller/EntityTypeController.java src/main/java/com/bedomain/controller/PropertyController.java</automated>
  </verify>
  <done>Controllers expose REST API endpoints</done>
</task>

</tasks>

<verification>
- [ ] POST /api/v1/entity-types creates entity type, returns 201
- [ ] GET /api/v1/entity-types returns paginated list
- [ ] GET /api/v1/entity-types/{id} returns entity type or 404
- [ ] PUT /api/v1/entity-types/{id} updates, returns 200
- [ ] DELETE /api/v1/entity-types/{id} marks deleted, returns 204
- [ ] POST /api/v1/entity-types/{id}/properties creates property with dataType
- [ ] GET /api/v1/entity-types/{id}/properties lists properties
- [ ] PUT /api/v1/entity-types/{id}/properties/{propId} updates property
- [ ] DELETE /api/v1/entity-types/{id}/properties/{propId} deletes property
- [ ] DataType supports STRING, NUMBER, DATE, BOOLEAN, TEXT
- [ ] Invalid requests return 400 with error details
</verification>

<success_criteria>
All CRUD endpoints for entity types and properties work correctly. Property specifications support all domain data types. Redis caching improves performance. Proper validation and error handling in place.
</success_criteria>

<output>
After completion, create `.planning/phases/01-entity-foundation/01-02b-SUMMARY.md`
