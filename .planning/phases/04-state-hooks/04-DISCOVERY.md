# Plan-Phase: Phase 04 - State Hooks (JavaScript Execution)

## Phase Overview

This phase adds the capability to execute JavaScript/TypeScript code when entering or exiting states in the state machine. The implementation uses GraalJS with sandboxed execution for security.

## Discovery: JavaScript Engine Options for JVM

### Research Summary

After evaluating options, **GraalJS** (via GraalVM Polyglot API) is selected:

| Option | Pros | Cons | Recommendation |
|--------|------|------|----------------|
| **GraalJS** | Modern, maintained by Oracle, sandbox support, good performance | Larger memory footprint | **SELECTED** |
| Nashorn (JDK 15+) | Built-in, fast | Deprecated, removed in JDK 15+ | Not viable |
| Rhino | Pure Java, no native deps | Slow, less maintained | Backup only |

### Sandboxing Strategy

Using **CONSTRAINED** sandbox policy (for trusted internal scripts):

```java
Context context = Context.newBuilder("js")
    .sandbox(SandboxPolicy.CONSTRAINED)
    .option("sandbox.MaxCPUTime", "2s")
    .option("sandbox.MaxStatements", "1000")
    .option("sandbox.MaxHeapMemory", "64MB")
    .build();
```

For untrusted scripts, use **UNTRUSTED** policy with stricter limits.

### Key Design Decisions

1. **Storage**: Scripts stored in `state_specs` table (onEnterScript, onExitScript columns)
2. **Timeout**: 2 seconds max execution time
3. **Error handling**: Script failure blocks transition (configurable)
4. **Context access**: Scripts receive entity attributes as JavaScript object
5. **Modification**: Scripts can modify attributes before transition completes

---

## Requirements

This phase addresses the following requirements:

- [ ] **HOOK-01**: State can have onEnter JavaScript code that executes when entering the state
- [ ] **HOOK-02**: State can have onExit JavaScript code that executes when exiting the state
- [ ] **HOOK-03**: Script has access to entity attributes (read/write)
- [ ] **HOOK-04**: Script execution is sandboxed for security
- [ ] **HOOK-05**: Script timeout prevents infinite loops
- [ ] **HOOK-06**: Script errors are logged and optionally block transition
- [ ] **HOOK-07**: Execution audit trail is maintained
