package com.bedomain.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

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
