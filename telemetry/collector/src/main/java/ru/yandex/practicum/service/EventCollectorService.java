package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.config.KafkaTopicConfig;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.model.hub.HubEvent;
import ru.yandex.practicum.model.sensor.SensorEvent;
import ru.yandex.practicum.serializer.AvroSerializer;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventCollectorService {
    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final AvroConverter avroConverter;
    private final AvroSerializer avroSerializer;
    private final KafkaTopicConfig topicConfig;

    public void collectSensorEvent(SensorEvent event) {
        SensorEventAvro avro = avroConverter.toSensorAvro(event);
        String topic = topicConfig.getSensors();
        byte[] payload = avroSerializer.serialize(topic, avro);

        log.info("Отправка события датчика в Kafka: topic={}, key={}, тип={}",
                topic, event.getId(), event.getType());

        kafkaTemplate.send(topic, event.getId(), payload);
    }

    public void collectHubEvent(HubEvent event) {
        HubEventAvro avro = avroConverter.toHubAvro(event);
        String topic = topicConfig.getHubs();
        byte[] payload = avroSerializer.serialize(topic, avro);

        log.info("Отправка события хаба в Kafka: topic={}, key={}, тип={}",
                topic, event.getHubId(), event.getType());

        kafkaTemplate.send(topic, event.getHubId(), payload);
    }
}
