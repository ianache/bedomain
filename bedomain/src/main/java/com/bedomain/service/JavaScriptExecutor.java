package com.bedomain.service;

import com.bedomain.config.JavaScriptConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.SandboxPolicy;
import org.graalvm.polyglot.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for executing user-defined JavaScript scripts in a sandboxed environment.
 * 
 * <p>Scripts have access to an {@code entity} object containing the entity attributes
 * and can modify them. The execution is sandboxed using GraalJS with configurable
 * resource limits.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JavaScriptExecutor {

    private final JavaScriptConfig config;

    /**
     * Execute a JavaScript script with the given context.
     *
     * @param script   The JavaScript code to execute
     * @param context The entity attributes as a map
     * @return The modified context after script execution
     * @throws ScriptExecutionException if script execution fails and failOnError is true
     */
    public Map<String, Object> execute(String script, Map<String, Object> context) {
        return execute(script, context, null, null);
    }

    /**
     * Execute a JavaScript script with additional metadata.
     *
     * @param script    The JavaScript code to execute
     * @param context   The entity attributes as a map
     * @param entityId  The ID of the entity (optional, for logging/metadata)
     * @param hookType  The type of hook ("onEnter" or "onExit") (optional)
     * @return The modified context after script execution
     * @throws ScriptExecutionException if script execution fails and failOnError is true
     */
    public Map<String, Object> execute(String script, Map<String, Object> context, 
                                       UUID entityId, String hookType) {
        // Add metadata to context
        Map<String, Object> fullContext = new HashMap<>(context);
        if (entityId != null) {
            fullContext.put("_entityId", entityId.toString());
        }
        if (hookType != null) {
            fullContext.put("_hookType", hookType);
        }
        fullContext.put("_timestamp", Instant.now().toString());

        try {
            return doExecute(script, fullContext);
        } catch (Exception e) {
            log.error("Script execution failed: script={}, entityId={}, hookType={}, error={}",
                script.substring(0, Math.min(100, script.length())),
                entityId, hookType, e.getMessage());

            if (config.isFailOnError()) {
                throw new ScriptExecutionException("Script execution failed: " + e.getMessage(), e);
            }
            // Return original context if failOnError is false
            return context;
        }
    }

    private Map<String, Object> doExecute(String script, Map<String, Object> context) {
        // Build sandboxed context
        Context.Builder builder = Context.newBuilder("js")
            .sandbox(toGraalSandboxPolicy(config.getPolicy()))
            .option("sandbox.MaxCPUTime", config.getMaxCpuTimeSeconds() + "s")
            .option("sandbox.MaxStatements", String.valueOf(config.getMaxStatements()))
            .option("sandbox.MaxHeapMemory", config.getMaxHeapMemoryMb() + "MB")
            .out(new ByteArrayOutputStream())
            .err(new ByteArrayOutputStream());

        try (Context context2 = builder.build()) {
            // Inject entity context
            Value bindings = context2.getBindings("js");
            bindings.putMember("entity", context);

            // Execute the script
            context2.eval("js", script);

            // Extract modified entity
            Value entityValue = bindings.getMember("entity");
            if (entityValue == null || entityValue.isNull()) {
                return context;
            }

            // Convert back to Map
            Map<String, Object> result = new HashMap<>();
            if (entityValue.hasMembers()) {
                for (String key : entityValue.getMemberKeys()) {
                    if (!key.startsWith("_")) { // Skip internal metadata
                        Value member = entityValue.getMember(key);
                        if (member.isString()) {
                            result.put(key, member.asString());
                        } else if (member.isNumber()) {
                            result.put(key, member.asDouble());
                        } else if (member.isBoolean()) {
                            result.put(key, member.asBoolean());
                        } else {
                            // For complex objects, try to convert to string
                            result.put(key, member.toString());
                        }
                    }
                }
            }

            return result.isEmpty() ? context : result;
        }
    }

    private SandboxPolicy toGraalSandboxPolicy(JavaScriptConfig.SandboxPolicy policy) {
        if (policy == null) {
            return SandboxPolicy.CONSTRAINED;
        }
        return switch (policy) {
            case NONE -> SandboxPolicy.NONE;
            case CONSTRAINED -> SandboxPolicy.CONSTRAINED;
            case STRICT -> SandboxPolicy.STRICT;
        };
    }

    /**
     * Exception thrown when script execution fails.
     */
    public static class ScriptExecutionException extends RuntimeException {
        public ScriptExecutionException(String message) {
            super(message);
        }

        public ScriptExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
