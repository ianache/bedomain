---
phase: 04-state-hooks
plan: 03
subsystem: state-transition
tags:
  - state-transition
  - hooks
  - audit
  - javascript
dependency_graph:
  requires:
    - 04-01 (StateSpec script fields)
    - 04-02 (JavaScriptExecutor)
  provides:
    - Hook execution in state transitions
    - Hook audit trail in StateHistory
  affects:
  - EntityInstance attributes
  - StateHistory records
tech_stack:
  added:
    - StateHistory hook fields
    - Hook execution in StateTransitionService
  patterns:
    - onExit before state change
    - onEnter after state change
    - SHA-256 script hash for audit
key_files:
  created: []
  modified:
    - bedomain/src/main/java/com/bedomain/service/StateTransitionService.java
    - bedomain/src/main/java/com/bedomain/service/StateHistoryService.java
    - bedomain/src/main/java/com/bedomain/domain/entity/StateHistory.java
    - bedomain/src/main/java/com/bedomain/domain/dto/statemachine/StateHistoryResponse.java
decisions:
  - "onExit executes before state change, onEnter after"
  - "SHA-256 hash of script stored for audit"
  - "Hook errors propagate based on failOnError config"
---

# Phase 4 Plan 3: Hook Integration in StateTransition Summary

## One-Liner

Integrated JavaScript hooks into state transition flow with full audit trail in StateHistory.

## Tasks Completed

| Task | Name | Commit |
|------|------|--------|
| 1 | Modify StateTransitionService to execute hooks | ecc2491 |
| 2 | Extend StateHistory for hook audit | ecc2491 |
| 3 | Add helper methods to JavaScriptExecutor | (completed in 04-02) |

## Key Changes

- **StateTransitionService.java**: 
  - Added JavaScriptExecutor dependency
  - Gets StateSpec for both fromState and toState
  - Executes onExit script before state change
  - Executes onEnter script after determining new state
  - Updated entity attributes after hook execution
  - Records history with hook metadata

- **StateHistory.java**: Added audit fields:
  - `hookExecuted`: boolean
  - `hookType`: String ("onEnter" or "onExit")
  - `hookScriptHash`: SHA-256 of script
  - `hookError`: Error message if hook failed

- **StateHistoryService.java**: Updated record() method to accept hook metadata

- **StateHistoryResponse.java**: Added hook fields to response DTO

## Execution Flow

1. Lookup EntityInstance
2. Get current state
3. Find valid transition
4. **Execute onExit script** (if exists)
5. **Execute onEnter script** (if exists)
6. Update entity.currentState
7. Record history (with hook info)
8. Publish Kafka event

## Error Handling

- If failOnError=true (default): ScriptExecutionException propagates, transaction rolls back
- If failOnError=false: Original context returned, transition proceeds

## Verification

- onExit executes when leaving a state
- onEnter executes when entering a state
- Modified attributes persist after hooks
- Hook failures logged in StateHistory

## Deviations

None - plan executed exactly as written.

---

## Self-Check: PASSED

All files modified exist, commits created successfully.
