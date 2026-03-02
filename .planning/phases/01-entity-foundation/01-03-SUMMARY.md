---
phase: 01-entity-foundation
plan: 03
subsystem: api
tags: [jpa, rest, crud, soft-delete, json]

# Dependency graph
requires:
  - phase: 01-entity-foundation
    provides: EntityType entity and repository (01-02b)
provides:
  - EntityInstance CRUD API endpoints
  - Soft delete support for entity instances
  - JSON attribute storage with type preservation
affects: [Phase 2 - State Management]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Soft delete pattern: deleted flag with repository query filters"
    - "JSON column storage with @JdbcTypeCode for Map attributes"
    - "ManyToOne lazy loading for entity type relationships"

key-files:
  created:
    - "bedomain/src/main/java/com/bedomain/domain/entity/EntityInstance.java"
    - "bedomain/src/main/java/com/bedomain/domain/dto/entityinstance/CreateEntityInstanceRequest.java"
    - "bedomain/src/main/java/com/bedomain/domain/dto/entityinstance/UpdateEntityInstanceRequest.java"
    - "bedomain/src/main/java/com/bedomain/domain/dto/entityinstance/EntityInstanceResponse.java"
  modified:
    - "bedomain/src/main/java/com/bedomain/repository/EntityInstanceRepository.java"
    - "bedomain/src/main/java/com/bedomain/service/EntityInstanceService.java"
    - "bedomain/src/main/java/com/bedomain/controller/EntityInstanceController.java"

key-decisions:
  - "Used soft delete instead of physical delete for data integrity"
  - "Included entityTypeName in response for convenience"

patterns-established:
  - "Soft delete pattern with @Builder.Default for boolean flags"

requirements-completed: [ENTY-01, ENTY-02, ENTY-03, ENTY-04, ENTY-05, ENTY-06]

# Metrics
duration: 6min
completed: 2026-03-02
---

# Phase 1 Plan 3: Entity Instance CRUD Summary

**EntityInstance CRUD with soft delete and JSON attributes, using ManyToOne relationship to EntityType**

## Performance

- **Duration:** 6 min
- **Started:** 2026-03-02T00:03:03Z
- **Completed:** 2026-03-02T00:09:35Z
- **Tasks:** 1 (implemented as single atomic unit)
- **Files modified:** 7 (3 created, 1 deleted, 3 modified)

## Accomplishments
- EntityInstance entity with ManyToOne relationship to EntityType
- JSON attribute storage using @JdbcTypeCode(SqlTypes.JSON)
- Soft delete support with deleted flag
- Complete CRUD REST API at /api/v1/entities
- Pagination support for list endpoints
- Entity type filtering support

## Task Commits

1. **Task 1-5: EntityInstance CRUD** - `ba3c518` (feat)
   - Created EntityInstance entity with JSON attributes and soft delete
   - Created DTOs in domain.dto.entityinstance package
   - Updated repository with soft delete queries
   - Updated service with soft delete implementation
   - Updated controller to use domain DTOs

**Plan metadata:** N/A (plan not committed separately)

## Files Created/Modified
- `bedomain/src/main/java/com/bedomain/domain/entity/EntityInstance.java` - JPA entity with JSON attributes, ManyToOne relationship, soft delete
- `bedomain/src/main/java/com/bedomain/domain/dto/entityinstance/CreateEntityInstanceRequest.java` - Create request DTO
- `bedomain/src/main/java/com/bedomain/domain/dto/entityinstance/UpdateEntityInstanceRequest.java` - Update request DTO
- `bedomain/src/main/java/com/bedomain/domain/dto/entityinstance/EntityInstanceResponse.java` - Response DTO with entityTypeName
- `bedomain/src/main/java/com/bedomain/repository/EntityInstanceRepository.java` - Added soft delete queries
- `bedomain/src/main/java/com/bedomain/service/EntityInstanceService.java` - Implemented soft delete, entityTypeName
- `bedomain/src/main/java/com/bedomain/controller/EntityInstanceController.java` - Uses domain DTOs

## Decisions Made
- Used soft delete (deleted flag) instead of physical delete for data integrity and audit trail
- Included entityTypeName in response for client convenience without additional lookup

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 2 - Missing Critical] Added soft delete support**
- **Found during:** Task 1 (Entity implementation)
- **Issue:** Plan specified soft delete but existing implementation used hard delete
- **Fix:** Added `deleted` boolean field with @Builder.Default, updated repository queries with AndDeletedFalse filters, modified service delete() to set deleted=true
- **Files modified:** EntityInstance.java, EntityInstanceRepository.java, EntityInstanceService.java
- **Verification:** Build succeeds, all queries filter by deleted=false
- **Committed in:** ba3c518

**2. [Rule 1 - Bug] Fixed package structure**
- **Found during:** Task 1 (Entity implementation)
- **Issue:** Files were in wrong packages (com.bedomain.entity instead of com.bedomain.domain.entity)
- **Fix:** Created new files in correct packages, removed old files
- **Files created/removed:** New files in domain/entity and domain/dto/entityinstance
- **Verification:** Build succeeds with correct imports
- **Committed in:** ba3c518

**3. [Rule 1 - Bug] Changed entityTypeId to ManyToOne relationship**
- **Found during:** Task 1 (Entity implementation)
- **Issue:** Implementation used UUID entityTypeId, plan specified @ManyToOne relationship
- **Fix:** Changed to @ManyToOne(fetch = LAZY) with EntityType, updated service to use entity.getId() and entity.getName()
- **Files modified:** EntityInstance.java, EntityInstanceService.java
- **Verification:** Build succeeds
- **Committed in:** ba3c518

**4. [Rule 2 - Missing Critical] Changed attributes from String to Map**
- **Found during:** Task 1 (Entity implementation)
- **Issue:** Implementation used String for attributes (JSON serialized), plan specified Map<String, Object>
- **Fix:** Changed to Map<String, Object> with @JdbcTypeCode(SqlTypes.JSON)
- **Files modified:** EntityInstance.java
- **Verification:** Build succeeds
- **Committed in:** ba3c518

**5. [Rule 1 - Bug] Added entityTypeName to response**
- **Found during:** Task 2 (DTOs)
- **Issue:** Response was missing entityTypeName field specified in plan
- **Fix:** Added entityTypeName field to EntityInstanceResponse
- **Files modified:** EntityInstanceResponse.java, EntityInstanceService.java
- **Verification:** Build succeeds
- **Committed in:** ba3c518

---

**Total deviations:** 5 auto-fixed (2 missing critical, 3 bugs)
**Impact on plan:** All deviations were necessary corrections to match plan specifications. No scope creep.

## Issues Encountered
- None - all issues were auto-fixed via deviation rules

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- EntityInstance CRUD complete with soft delete
- Ready for Phase 2: State Management
- EntityType and EntityInstance foundations complete for Phase 1

---
*Phase: 01-entity-foundation*
*Completed: 2026-03-02*
