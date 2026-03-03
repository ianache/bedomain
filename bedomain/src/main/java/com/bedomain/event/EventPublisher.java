package com.bedomain.event;

import com.bedomain.domain.entity.EntityInstance;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPIC_ENTITIES_CREATED = "bedomain.entities.created";
    private static final String TOPIC_ENTITIES_UPDATED = "bedomain.entities.updated";
    private static final String TOPIC_STATE_CHANGED = "bedomain.entities.state-changed";

    private static final String EVENT_TYPE_CREATED = "com.bedomain.entity.created";
    private static final String EVENT_TYPE_UPDATED = "com.bedomain.entity.updated";
    private static final String EVENT_TYPE_STATE_CHANGED = "com.bedomain.entity.state-changed";

    private static final URI EVENT_SOURCE = URI.create("/bedomain");

    public void publishEntityCreated(EntityInstance entity) {
        EntityEvent eventData = EntityEvent.builder()
                .entityId(entity.getId())
                .entityType(entity.getEntityType().getName())
                .attributes(entity.getAttributes())
                .timestamp(Instant.now())
                .user(entity.getCreatedBy())
                .build();

        publish(TOPIC_ENTITIES_CREATED, EVENT_TYPE_CREATED, eventData, entity.getId().toString());
    }

    public void publishEntityUpdated(EntityInstance entity) {
        EntityEvent eventData = EntityEvent.builder()
                .entityId(entity.getId())
                .entityType(entity.getEntityType().getName())
                .attributes(entity.getAttributes())
                .timestamp(Instant.now())
                .user(entity.getUpdatedBy())
                .build();

        publish(TOPIC_ENTITIES_UPDATED, EVENT_TYPE_UPDATED, eventData, entity.getId().toString());
    }

    public void publishStateChanged(EntityInstance entity, String previousState, String newState, String user) {
        EntityEvent eventData = EntityEvent.builder()
                .entityId(entity.getId())
                .entityType(entity.getEntityType().getName())
                .attributes(entity.getAttributes())
                .previousState(previousState)
                .newState(newState)
                .timestamp(Instant.now())
                .user(user)
                .build();

        publish(TOPIC_STATE_CHANGED, EVENT_TYPE_STATE_CHANGED, eventData, entity.getId().toString());
    }

    private void publish(String topic, String eventType, EntityEvent eventData, String key) {
        try {
            CloudEvent cloudEvent = CloudEventBuilder.v1()
                    .withId(UUID.randomUUID().toString())
                    .withType(eventType)
                    .withSource(EVENT_SOURCE)
                    .withTime(OffsetDateTime.now(ZoneOffset.UTC))
                    .withDataContentType("application/json")
                    .withData(objectMapper.writeValueAsBytes(eventData))
                    .build();

            kafkaTemplate.send(topic, key, cloudEvent.toString())
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish event to {}: {}", topic, ex.getMessage());
                        } else {
                            log.debug("Published event {} to {}", cloudEvent.getId(), topic);
                        }
                    });
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event data for {}: {}", topic, e.getMessage());
        }
    }
}
