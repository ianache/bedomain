# Phase 3: Event Publishing - Research

**Researched:** 2026-03-02
**Domain:** Spring Kafka + CloudEvents Integration
**Confidence:** HIGH

## Summary

This phase implements event publishing to Kafka using CloudEvents format. The research confirms Spring Kafka (spring-kafka 3.2.x) integrates well with Spring Boot 3.2.0, and CloudEvents Java SDK (4.0.1) provides standard event formatting. The key architectural decision is using fire-and-forget async publishing with log-and-continue error handling, which aligns with the user's requirements for minimal latency impact.

**Primary recommendation:** Use Spring Kafka's KafkaTemplate with async send() for non-blocking publishing, CloudEvents SDK for standard event format, and a dedicated EventPublisher service that integrates with existing services (EntityInstanceService, StateTransitionService).

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

- **Event Format:** CloudEvents — Industry standard JSON envelope with specversion, type, source, id, data
- **Topic Design:** Domain-based topics — `bedomain.{domain}.{event}` (e.g., `bedomain.entities.created`, `bedomain.entities.updated`, `bedomain.entities.state-changed`)
- **Delivery Guarantee:** Fire-and-forget — Send to Kafka without waiting for ACK
- **Error Handling:** Log and continue — If Kafka fails, log the error and proceed with the operation
- **Publishing:** Asynchronous — Return immediately after sending to Kafka

### Claude's Discretion

None — all implementation decisions are locked in Context.md

### Deferred Ideas (OUT OF SCOPE)

- None — discussion stayed within phase scope
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|-----------------|
| EVNT-01 | System publishes business event to Kafka on state transition | KafkaTemplate async send + CloudEvents format + StateTransitionService integration point |
| EVNT-02 | System publishes business event to Kafka on entity creation | KafkaTemplate async send + CloudEvents format + EntityInstanceService.create() hook |
| EVNT-03 | System publishes business event to Kafka on entity update | KafkaTemplate async send + CloudEvents format + EntityInstanceService.update() hook |
| EVNT-04 | Event includes entity ID, type, previous state, new state, timestamp, user | CloudEvents data payload structure + existing JwtAuthenticationService |
| INFRA-03 | Application connects to Kafka for event publishing | spring-kafka dependency + application.yml config |
</phase_requirements>

## Standard Stack

### Core

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| spring-kafka | 3.2.0 | Kafka producer/consumer integration | Official Spring integration, auto-config with Spring Boot |
| cloudevents-core | 4.0.1 | CloudEvent building and manipulation | CNCF standard, official SDK |
| cloudevents-json-jackson | 4.0.1 | JSON serialization for CloudEvents | Required for JSON event format |
| cloudevents-kafka | 4.0.1 | Kafka protocol binding for CloudEvents | Optional - simplifies Kafka CloudEvent sending |

### Supporting

| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| spring-kafka-test | 3.2.0 | Embedded Kafka for testing | Integration tests |
| kafka-clients | 3.7.0 | Kafka producer/consumer clients | Transitive from spring-kafka |

### Alternatives Considered

| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| CloudEvents SDK | Manual JSON construction | SDK provides validation and standard attributes; manual is simpler but less standard |
| KafkaTemplate | Direct KafkaProducer | KafkaTemplate is Spring-managed, easier DI and testing |
| Synchronous send | KafkaTemplate.send().get() | Sync blocks thread; async required for fire-and-forget |

**Installation:**
```xml
<!-- pom.xml additions -->
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
<dependency>
    <groupId>io.cloudevents</groupId>
    <artifactId>cloudevents-core</artifactId>
    <version>4.0.1</version>
</dependency>
<dependency>
    <groupId>io.cloudevents</groupId>
    <artifactId>cloudevents-json-jackson</artifactId>
    <version>4.0.1</version>
</dependency>
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka-test</artifactId>
    <scope>test</scope>
</dependency>
```

## Architecture Patterns

### Recommended Project Structure
```
src/main/java/com/bedomain/
├── config/
│   └── KafkaConfig.java          # Kafka producer configuration
├── event/
│   ├── EventPublisher.java       # Async event publishing service
│   └── EntityEvent.java          # Event data DTO
├── service/
│   ├── EntityInstanceService.java  # Add event publishing hook
│   └── StateTransitionService.java  # Add event publishing hook
```

### Pattern 1: Async Fire-and-Forget Kafka Publishing
**What:** Use KafkaTemplate.send() which returns CompletableFuture without blocking
**When to use:** When minimal latency is required and message loss is acceptable
**Example:**
```java
// Source: https://docs.spring.io/spring-kafka/reference/
@Service
@RequiredArgsConstructor
public class EventPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    
    public void publishAsync(String topic, String key, CloudEvent event) {
        kafkaTemplate.send(topic, key, event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to send event to {}: {}", topic, ex.getMessage());
                } else {
                    log.debug("Event sent to {}", topic);
                }
            });
    }
}
```

### Pattern 2: CloudEvent Building
**What:** Use CloudEventBuilder to create standard CloudEvents
**When to use:** For all published events to ensure format consistency
**Example:**
```java
// Source: https://cloudevents.github.io/sdk-java/core.html
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import java.net.URI;

CloudEvent event = CloudEventBuilder.v1()
    .withId(UUID.randomUUID().toString())
    .withType("com.bedomain.entity.created")
    .withSource(URI.create("/bedomain"))
    .withTime(Instant.now())
    .withData(jsonBytes)
    .build();
```

### Pattern 3: Service Integration Point
**What:** Call event publisher after successful database operations
**When to use:** In EntityInstanceService.create(), update(), and StateTransitionService.triggerTransition()
**Example:**
```java
@Transactional
public EntityInstanceResponse create(CreateEntityInstanceRequest request) {
    // ... existing save logic
    entityInstance = entityInstanceRepository.save(entityInstance);
    
    // Publish event after successful save
    eventPublisher.publishEntityCreated(entityInstance);
    
    return toResponse(entityInstance);
}
```

### Anti-Patterns to Avoid
- **Synchronous wait:** Using kafkaTemplate.send().get() blocks thread, defeats async purpose
- **Event inside transaction:** Publishing after transaction commits prevents inconsistent state if rollback occurs
- **No error handling:** Fire-and-forget requires logging at minimum for monitoring

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Event serialization | Manual JSON building | CloudEvents SDK | Standard attributes, validation, Jackson integration |
| Kafka configuration | Manual ProducerFactory beans | Spring Boot auto-config | Less code, tested defaults |
| Topic creation | Runtime topic management | NewTopic @Bean | Spring manages lifecycle |

**Key insight:** CloudEvents SDK handles spec compliance, JSON serialization, and provides type safety. Building events manually risks non-standard format and harder downstream consumption.

## Common Pitfalls

### Pitfall 1: Blocking on Kafka Send
**What goes wrong:** Using send().get() synchronously defeats fire-and-forget, adds latency
**Why it happens:** Developers familiar with sync APIs default to blocking calls
**How to avoid:** Always use CompletableFuture with callbacks, never call .get()
**Warning signs:** API response times correlate with Kafka broker latency

### Pitfall 2: Publishing Before Transaction Commits
**What goes wrong:** Event published but DB transaction rolls back, causing inconsistency
**Why it happens:** Placing event publish inside @Transactional method before commit
**How to avoid:** Use TransactionalEventListener with AFTER_COMMIT phase, or publish after service method returns
**Warning signs:** Downstream consumers see events for entities that don't exist

### Pitfall 3: Losing Events on Application Crash
**What goes wrong:** Fire-and-forget sends may be lost if app crashes before broker acknowledges
**Why it happens:** No acknowledgment waiting + broker unreachable
**How to avoid:** This is acceptable per requirements (fire-and-forget). Log failures for monitoring to detect patterns
**Warning signs:** Missing events in consumer without error logs

### Pitfall 4: Missing Topic Configuration
**What goes wrong:** Events fail to send because topics don't exist
**Why it happens:** Kafka auto-create disabled or misconfigured
**How to avoid:** Add NewTopic @Bean for each topic in KafkaConfig
**Warning signs:** org.apache.kafka.common.errors.TopicAuthorizationException

## Code Examples

### KafkaConfig.java
```java
// Source: https://docs.spring.io/spring-boot/reference/messaging/kafka.html
@Configuration
public class KafkaConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Bean
    public NewTopic entitiesCreatedTopic() {
        return TopicBuilder.name("bedomain.entities.created")
            .partitions(3)
            .replicas(1)
            .build();
    }
    
    @Bean
    public NewTopic entitiesUpdatedTopic() {
        return TopicBuilder.name("bedomain.entities.updated")
            .partitions(3)
            .replicas(1)
            .build();
    }
    
    @Bean
    public NewTopic entitiesStateChangedTopic() {
        return TopicBuilder.name("bedomain.entities.state-changed")
            .partitions(3)
            .replicas(1)
            .build();
    }
}
```

### EventPublisher Service
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    public void publishEntityCreated(EntityInstance entity) {
        publish("bedomain.entities.created", entity.getId().toString(), 
            buildCloudEvent("com.bedomain.entity.created", entity));
    }
    
    public void publishEntityUpdated(EntityInstance entity) {
        publish("bedomain.entities.updated", entity.getId().toString(),
            buildCloudEvent("com.bedomain.entity.updated", entity));
    }
    
    public void publishStateChanged(EntityInstance entity, String previousState, String newState, String user) {
        publish("bedomain.entities.state-changed", entity.getId().toString(),
            buildStateChangeCloudEvent(entity, previousState, newState, user));
    }
    
    private void publish(String topic, String key, CloudEvent event) {
        kafkaTemplate.send(topic, key, event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish event to {}: {}", topic, ex.getMessage());
                } else {
                    log.debug("Published event {} to {}", event.getId(), topic);
                }
            });
    }
    
    private CloudEvent buildCloudEvent(String type, EntityInstance entity) {
        EntityEventData data = EntityEventData.builder()
            .entityId(entity.getId())
            .entityType(entity.getEntityType().getName())
            .attributes(entity.getAttributes())
            .timestamp(Instant.now())
            .user(entity.getCreatedBy())
            .build();
        
        return CloudEventBuilder.v1()
            .withId(UUID.randomUUID().toString())
            .withType(type)
            .withSource(URI.create("/bedomain"))
            .withTime(Instant.now())
            .withDataContentType("application/json")
            .withData(toJsonBytes(data))
            .build();
    }
}
```

### application.yml Configuration
```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: 0                    # Fire-and-forget - no acknowledgment
      retries: 0                 # No retries per requirements
      properties:
        linger.ms: 0              # Send immediately
        max.block.ms: 5000        # Max time to wait for buffer
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|-------------------|--------------|--------|
| Custom JSON events | CloudEvents SDK | 2024 (CE v1.0) | Industry standard, interoperability |
| Synchronous Kafka send | Async fire-and-forget | Spring Kafka 2.x+ | Reduced latency |
| XML configuration | Java @Bean + YAML | Spring Boot 2.1+ | Type-safe, IDE support |

**Deprecated/outdated:**
- Spring Kafka 2.x: Older API, but backward compatible
- Kafka 2.x client: Upgrade to 3.x for latest features

## Open Questions

1. **Kafka Bootstrap Server Configuration**
   - What we know: Needs `spring.kafka.bootstrap-servers` in config
   - What's unclear: Whether dev/prod environments have Kafka available
   - Recommendation: Add KafkaConfig with conditional @Profile to disable if Kafka unavailable

2. **Entity State Storage**
   - What we know: EntityInstance has currentState field (from Phase 2)
   - What's unclear: How to get previousState for event - need to store before update
   - Recommendation: Pass previousState as parameter to event publishing method

3. **Testing Without Real Kafka**
   - What we know: spring-kafka-test provides @EmbeddedKafka
   - What's unclear: Whether project wants embedded or mocked for unit tests
   - Recommendation: Use @EmbeddedKafka for integration tests, mock EventPublisher for unit tests

## Sources

### Primary (HIGH confidence)
- Spring Boot Kafka Reference - https://docs.spring.io/spring-boot/reference/messaging/kafka.html
- CloudEvents Java SDK Core - https://cloudevents.github.io/sdk-java/core.html
- CloudEvents Java SDK Kafka - https://cloudevents.github.io/sdk-java/kafka.html

### Secondary (MEDIUM confidence)
- Spring Kafka Testing - https://www.baeldung.com/spring-boot-kafka-testing
- Kafka Error Handling Patterns - https://www.confluent.io/blog/error-handling-patterns-in-kafka/

### Tertiary (LOW confidence)
- Embedded Kafka Best Practices - Various blog posts, verified against official docs

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - Spring Kafka + CloudEvents SDK are well-documented and standard
- Architecture: HIGH - Patterns align with user decisions (fire-and-forget, async)
- Pitfalls: HIGH - Common issues well-documented in Spring/Kafka community

**Research date:** 2026-03-02
**Valid until:** 2026-04-02 (30 days - stable technology stack)
