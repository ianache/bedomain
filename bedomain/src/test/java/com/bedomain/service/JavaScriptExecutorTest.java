package com.bedomain.service;

import com.bedomain.config.JavaScriptConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JavaScriptExecutor.
 * 
 * Note: These tests require GraalJS to be available at runtime.
 * They are disabled by default and can be run with GraalJS in classpath.
 */
@Disabled("Requires GraalJS runtime - enable when running with GraalJS dependencies")
class JavaScriptExecutorTest {

    private JavaScriptExecutor executor;
    private JavaScriptConfig config;
    private UUID entityId;

    @BeforeEach
    void setUp() {
        config = new JavaScriptConfig();
        config.setMaxCpuTimeSeconds(2);
        config.setMaxStatements(1000);
        config.setMaxHeapMemoryMb(64);
        config.setFailOnError(true);
        config.setPolicy(JavaScriptConfig.SandboxPolicy.CONSTRAINED);
        
        executor = new JavaScriptExecutor(config);
        entityId = UUID.randomUUID();
    }

    @Test
    void execute_ScriptModifiesAttribute_ReturnsModifiedContext() {
        Map<String, Object> context = Map.of("status", "pending", "amount", 100);
        String script = "entity.amount = entity.amount * 1.1; entity.processed = true;";

        Map<String, Object> result = executor.execute(script, context, entityId, "onEnter");

        assertNotNull(result);
        assertEquals(110.0, result.get("amount"));
        assertEquals(true, result.get("processed"));
    }

    @Test
    void execute_ScriptReadsAttributes_CanAccessEntityData() {
        Map<String, Object> context = Map.of("name", "test-entity", "version", 1);
        String script = "entity.newField = 'Hello ' + entity.name;";

        Map<String, Object> result = executor.execute(script, context, entityId, "onEnter");

        assertEquals("Hello test-entity", result.get("newField"));
    }

    @Test
    void execute_InfiniteLoop_TimesOut() {
        String infiniteLoop = "while(true) {}";

        assertThrows(JavaScriptExecutor.ScriptExecutionException.class, () ->
            executor.execute(infiniteLoop, Map.of(), entityId, "onEnter"));
    }

    @Test
    void execute_ExcessStatements_Rejected() {
        // Create a script with more statements than allowed
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 2000; i++) {
            sb.append("entity.x").append(i).append(" = ").append(i).append(";");
        }
        String script = sb.toString();

        assertThrows(JavaScriptExecutor.ScriptExecutionException.class, () ->
            executor.execute(script, Map.of(), entityId, "onEnter"));
    }

    @Test
    void execute_SyntaxError_ThrowsException() {
        String badScript = "entity.x = ;;;";
        Map<String, Object> context = Map.of("x", 1);

        assertThrows(JavaScriptExecutor.ScriptExecutionException.class, () ->
            executor.execute(badScript, context, entityId, "onEnter"));
    }

    @Test
    void execute_FailOnErrorFalse_ReturnsOriginalContext() {
        config.setFailOnError(false);
        
        String badScript = "entity.x = ;;;";
        Map<String, Object> context = Map.of("x", 1, "y", 2);

        Map<String, Object> result = executor.execute(badScript, context, entityId, "onEnter");

        // Should return original context
        assertEquals(context, result);
    }

    @Test
    void execute_RuntimeError_HandledGracefully() {
        config.setFailOnError(false);
        
        String errorScript = "throw new Error('test error');";
        Map<String, Object> context = Map.of("x", 1);

        Map<String, Object> result = executor.execute(errorScript, context, entityId, "onEnter");

        // Should return original context
        assertEquals(context, result);
    }

    @Test
    void execute_MetadataAddedToContext() {
        String script = "entity.hasMetadata = (entity._entityId !== undefined && entity._hookType !== undefined);";
        
        Map<String, Object> result = executor.execute(script, Map.of(), entityId, "onEnter");

        assertEquals(true, result.get("hasMetadata"));
    }

    @Test
    void execute_EmptyScript_ReturnsOriginalContext() {
        Map<String, Object> context = Map.of("x", 1);
        
        Map<String, Object> result = executor.execute("", context, entityId, "onEnter");

        assertEquals(context, result);
    }

    @Test
    void execute_NullScript_ReturnsOriginalContext() {
        Map<String, Object> context = Map.of("x", 1);
        
        Map<String, Object> result = executor.execute(null, context, entityId, "onEnter");

        assertEquals(context, result);
    }
}
