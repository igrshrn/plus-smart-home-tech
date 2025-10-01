package ru.yandex.practicum.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.serializer.AvroSerializer;

import java.time.Instant;

@Slf4j
@Component
public class KafkaEventProducer {

    private final KafkaProducer<String, byte[]> producer;
    private final String sensorTopic;
    private final String hubTopic;

    public KafkaEventProducer(
            KafkaProducer<String, byte[]> producer,
            KafkaTopicConfig topicConfig
    ) {
        this.producer = producer;
        this.sensorTopic = topicConfig.getSensors();
        this.hubTopic = topicConfig.getHubs();
    }

    private void send(SpecificRecordBase event, String key, Instant timestamp, String topic) {
        byte[] value = new AvroSerializer().serialize(topic, event);

        ProducerRecord<String, byte[]> record = new ProducerRecord<>(
                topic,
                null,
                timestamp.toEpochMilli(),
                key,
                value
        );

        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error("Ошибка отправки в Kafka топик {}: {}", topic, exception.getMessage(), exception);
            } else {
                log.debug("Отправлено в Kafka: топик={}, ключ={}, offset={}", topic, key, metadata.offset());
            }
        });
    }

    public void sendSensorEvent(SpecificRecordBase event, String key, Instant timestamp) {
        send(event, key, timestamp, sensorTopic);
    }

    public void sendHubEvent(SpecificRecordBase event, String key, Instant timestamp) {
        send(event, key, timestamp, hubTopic);
    }

}
