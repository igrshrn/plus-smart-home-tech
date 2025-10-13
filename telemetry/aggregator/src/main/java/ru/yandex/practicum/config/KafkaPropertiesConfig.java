package ru.yandex.practicum.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "aggregator.kafka")
public class KafkaPropertiesConfig {
    private Map<String, String> consumer;
    private Map<String, String> producer;
    private Duration consumerPollTimeout = Duration.ofSeconds(1);

    private Topics topics = new Topics();

    @Getter
    @Setter
    public static class Topics {
        private String sensors = "telemetry.sensors.v1";
        private String snapshots = "telemetry.snapshots.v1";
    }
}
