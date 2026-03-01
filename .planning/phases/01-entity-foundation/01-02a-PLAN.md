---
phase: 01-entity-foundation
plan: 02a
type: execute
wave: 2
depends_on: ["01-01"]
files_modified:
  - src/main/java/com/bedomain/domain/entity/EntityType.java
  - src/main/java/com/bedomain/domain/entity/Property.java
  - src/main/java/com/bedomain/domain/enums/DataType.java
  - src/main/java/com/bedomain/domain/dto/entitytype/CreateEntityTypeRequest.java
  - src/main/java/com/bedomain/domain/dto/entitytype/UpdateEntityTypeRequest.java
  - src/main/java/com/bedomain/domain/dto/entitytype/EntityTypeResponse.java
  - src/main/java/com/bedomain/domain/dto/property/CreatePropertyRequest.java
  - src/main/java/com/bedomain/domain/dto/property/UpdatePropertyRequest.java
  - src/main/java/com/bedomain/domain/dto/property/PropertyResponse.java
  - src/main/java/com/bedomain/repository/EntityTypeRepository.java
  - src/main/java/com/bedomain/repository/PropertyRepository.java
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
    - "Entity type JPA entity exists with UUID, name, description, deleted flag"
    - "Property JPA entity exists with DataType enum"
    - "DTOs for creating, updating, and responding with entity types and properties"
    - "Repositories with query methods for entity types and properties"
  artifacts:
    - path: "src/main/java/com/bedomain/domain/entity/EntityType.java"
      provides: "Entity type JPA entity with audit fields"
      contains: "@Entity, @Id UUID, name, description, deleted flag"
    - path: "src/main/java/com/bedomain/domain/entity/Property.java"
      provides: "Property specification JPA entity with data type enum"
      contains: "@Entity, DataType enum, ManyToOne EntityType"
    - path: "src/main/java/com/bedomain/domain/enums/DataType.java"
      provides: "Domain data types"
      contains: "STRING, NUMBER, DATE, BOOLEAN, TEXT"
    - path: "src/main/java/com/bedomain/repository/EntityTypeRepository.java"
      provides: "Entity type repository with query methods"
      contains: "JpaRepository, findByName, findByDeletedFalse"
    - path: "src/main/java/com/bedomain/repository/PropertyRepository.java"
      provides: "Property repository with query methods"
      contains: "JpaRepository, findByEntityTypeId"
  key_links:
    - from: "Property"
      to: "EntityType"
      via: "ManyToOne relationship"
      property: "entityType"
---

<objective>
Create domain entities (EntityType, Property, DataType enum), DTOs, and repositories for entity types and property specifications. This establishes the data layer for Phase 1.
</objective>

<context>
@.planning/phases/01-entity-foundation/01-CONTEXT.md
@.planning/phases/01-entity-foundation/01-RESEARCH.md

**Key constraints from CONTEXT.md:**
- DDD with SpringBoot 3.x + Java 21
- JPA/Hibernate with MySQL 8.x
- UUID primary keys (server-side generation)
- Soft deletes (deleted flag)
- Audit fields: createdAt, createdBy, updatedAt, updatedBy
- Redis cache-aside pattern for entity type lookups

**Technology from RESEARCH.md:**
- Use @GeneratedValue(generator = "UUID") with @GenericGenerator for UUID generation
- Use @EnableJpaAuditing for automatic audit fields
</context>

<tasks>

<task type="auto">
  <name>Task 1: Create domain entities (EntityType, Property, DataType enum)</name>
  <files>src/main/java/com/bedomain/domain/entity/EntityType.java, src/main/java/com/bedomain/domain/entity/Property.java, src/main/java/com/bedomain/domain/enums/DataType.java</files>
  <action>
    Create EntityType.java:
    - @Entity @Table(name = "entity_types") extending Auditable
    - @Id @GeneratedValue(generator = "UUID") @GenericGenerator for UUID
    - @Column(nullable=false, unique=true) String name
    - String description
    - @Column(nullable=false) boolean deleted = false
    - @OneToMany(mappedBy="entityType", cascade=ALL, orphanRemoval=true) List<Property> properties

    Create Property.java:
    - @Entity @Table(name = "properties")
    - UUID id, String name, String description
    - @Enumerated(EnumType.STRING) @Column(nullable=false) DataType dataType
    - @ManyToOne(fetch=LAZY) @JoinColumn EntityType entityType

    Create DataType.java enum with STRING, NUMBER, DATE, BOOLEAN, TEXT
  </action>
  <verify>
    <automated>grep -n "@Entity\|enum DataType\|STRING\|NUMBER\|DATE\|BOOLEAN\|TEXT" src/main/java/com/bedomain/domain/entity/EntityType.java src/main/java/com/bedomain/domain/entity/Property.java src/main/java/com/bedomain/domain/enums/DataType.java</automated>
  </verify>
  <done>EntityType, Property entities and DataType enum created</done>
</task>

<task type="auto">
  <name>Task 2: Create DTOs for Entity Type and Property</name>
  <files>src/main/java/com/bedomain/domain/dto/entitytype/CreateEntityTypeRequest.java, src/main/java/com/bedomain/domain/dto/entitytype/UpdateEntityTypeRequest.java, src/main/java/com/bedomain/domain/dto/entitytype/EntityTypeResponse.java, src/main/java/com/bedomain/domain/dto/property/CreatePropertyRequest.java, src/main/java/com/bedomain/domain/dto/property/UpdatePropertyRequest.java, src/main/java/com/bedomain/domain/dto/property/PropertyResponse.java</files>
  <action>
    Create DTOs using Java records or @Data classes:
    - CreateEntityTypeRequest: @NotBlank @Size(max=100) String name, @Size(max=500) String description
    - UpdateEntityTypeRequest: Optional<String> name, Optional<String> description
    - EntityTypeResponse: UUID id, String name, String description, List<PropertyResponse> properties, Instant createdAt, String createdBy

    - CreatePropertyRequest: @NotBlank String name, @Size(max=500) String description, @NotNull DataType dataType
    - UpdatePropertyRequest: Optional<String> name, Optional<String> description, Optional<DataType> dataType
    - PropertyResponse: UUID id, String name, String description, DataType dataType
  </action>
  <verify>
    <automated>ls src/main/java/com/bedomain/domain/dto/entitytype/ src/main/java/com/bedomain/domain/dto/property/ 2>/dev/null</automated>
  </verify>
  <done>All DTO classes created</done>
</task>

<task type="auto">
  <name>Task 3: Create repositories</name>
  <files>src/main/java/com/bedomain/repository/EntityTypeRepository.java, src/main/java/com/bedomain/repository/PropertyRepository.java</files>
  <action>
    Create EntityTypeRepository.java:
    - extends JpaRepository<EntityType, UUID>
    - Optional<EntityType> findByName(String name)
    - Page<EntityType> findByDeletedFalse(Pageable pageable)
    - boolean existsByNameAndDeletedFalse(String name)

    Create PropertyRepository.java:
    - extends JpaRepository<Property, UUID>
    - List<Property> findByEntityTypeId(UUID entityTypeId)
    - Optional<Property> findByIdAndEntityTypeId(UUID id, UUID entityTypeId)
  </action>
  <verify>
    <automated>grep -n "extends JpaRepository\|findBy\|Page<EntityType>\|List<Property/com/bedomain>" src/main/java/repository/EntityTypeRepository.java src/main/java/com/bedomain/repository/PropertyRepository.java</automated>
  </verify>
  <done>Repositories created with query methods</done>
</task>

</tasks>

<verification>
- [ ] EntityType entity compiles and has UUID id, name, description, deleted flag
- [ ] Property entity compiles and has DataType enum field
- [ ] DataType enum contains STRING, NUMBER, DATE, BOOLEAN, TEXT
- [ ] All DTOs exist with proper validation annotations
- [ ] EntityTypeRepository has findByName, findByDeletedFalse methods
- [ ] PropertyRepository has findByEntityTypeId method
</verification>

<success_criteria>
Domain entities, DTOs, and repositories are created and ready for service layer consumption.
</success_criteria>

<output>
After completion, create `.planning/phases/01-entity-foundation/01-02a-SUMMARY.md`
