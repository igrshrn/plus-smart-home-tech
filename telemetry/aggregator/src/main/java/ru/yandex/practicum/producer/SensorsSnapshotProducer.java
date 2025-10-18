package ru.yandex.practicum.producer;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorsSnapshotProducer {

    private final Producer<String, SensorsSnapshotAvro> producer;

    public void send(String topic, String key, SensorsSnapshotAvro message) {
        ProducerRecord<String, SensorsSnapshotAvro> record = new ProducerRecord<>(topic, key, message);
        producer.send(record, callback(key));
    }

    private Callback callback(String key) {
        return (RecordMetadata metadata, Exception exception) -> {
            if (exception != null) {
                log.error("Ошибка при отправке снапшота в Kafka. Ключ: {}, причина: {}", key, exception.getMessage(), exception);
            } else {
                log.debug("Снапшот успешно отправлен в топик '{}', раздел {}, смещение {}",
                        metadata.topic(), metadata.partition(), metadata.offset());
            }
        };
    }

    @PreDestroy
    void shutdown() {
        log.info("Завершение работы продюсера снапшотов: очистка буферов и закрытие соединения");
        try {
            producer.flush();
            producer.close();
            log.info("Продюсер снапшотов успешно остановлен.");
        } catch (Exception e) {
            log.warn("Ошибка при завершении работы продюсера: {}", e.getMessage(), e);
        }
    }
}