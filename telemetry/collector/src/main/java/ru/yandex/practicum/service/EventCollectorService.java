package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.config.KafkaEventProducer;
import ru.yandex.practicum.config.KafkaTopicConfig;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.model.hub.HubEvent;
import ru.yandex.practicum.model.sensor.SensorEvent;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventCollectorService {
    private final KafkaEventProducer kafkaEventProducer;
    private final AvroConverter avroConverter;
    private final KafkaTopicConfig topicConfig;

    public void collectSensorEvent(SensorEvent event) {
        SensorEventAvro avro = avroConverter.toSensorAvro(event);
        String topic = topicConfig.getSensors();
        kafkaEventProducer.sendSensorEvent(avro, event.getId(), event.getTimestamp());

        log.info("Отправка события датчика в Kafka: topic={}, key={}, тип={}",
                topic, event.getId(), event.getType());
    }

    public void collectHubEvent(HubEvent event) {
        HubEventAvro avro = avroConverter.toHubAvro(event);
        kafkaEventProducer.sendHubEvent(avro, event.getHubId(), event.getTimestamp());
        String topic = topicConfig.getHubs();

        log.info("Отправка события хаба в Kafka: topic={}, key={}, тип={}",
                topic, event.getHubId(), event.getType());
    }
}
