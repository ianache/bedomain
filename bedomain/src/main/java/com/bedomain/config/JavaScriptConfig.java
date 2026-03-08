package com.bedomain.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.javascript")
@Getter
@Setter
public class JavaScriptConfig {

    /**
     * Maximum CPU time in seconds for script execution
     */
    private int maxCpuTimeSeconds = 2;

    /**
     * Maximum number of statements allowed in a script
     */
    private int maxStatements = 1000;

    /**
     * Maximum heap memory in MB for script execution
     */
    private int maxHeapMemoryMb = 64;

    /**
     * Whether to fail on script errors (throw exception) or return original context
     */
    private boolean failOnError = true;

    /**
     * Sandbox policy: NONE, CONSTRAINED, or STRICT
     */
    private SandboxPolicy policy = SandboxPolicy.CONSTRAINED;

    public enum SandboxPolicy {
        NONE,
        CONSTRAINED,
        STRICT
    }
}
