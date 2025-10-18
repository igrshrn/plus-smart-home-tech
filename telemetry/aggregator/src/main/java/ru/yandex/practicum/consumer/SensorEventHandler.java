package ru.yandex.practicum.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.config.KafkaPropertiesConfig;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.producer.SensorsSnapshotProducer;
import ru.yandex.practicum.service.AggregationService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SensorEventHandler {

    private final AggregationService aggregationService;
    private final SensorsSnapshotProducer producer;
    private final KafkaConsumer<String, SensorEventAvro> consumer;
    private final KafkaPropertiesConfig kafkaProperties;
    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    public void handle(ConsumerRecords<String, SensorEventAvro> records) {
        if (records.isEmpty()) {
            return;
        }

        String snapshotsTopic = kafkaProperties.getTopics().getSnapshots();

        for (ConsumerRecord<String, SensorEventAvro> record : records) {
            aggregationService.aggregateEvent(record.value())
                    .ifPresentOrElse(
                            snapshot -> {
                                log.info("Обнаружено изменение состояния хаба '{}'. Отправка снапшота в Kafka.", snapshot.getHubId());
                                producer.send(snapshotsTopic, snapshot.getHubId(), snapshot);
                            },
                            () -> log.trace("Событие от датчика {} проигнорировано: состояние не изменилось.", record.value().getId())
                    );

            currentOffsets.put(
                    new TopicPartition(record.topic(), record.partition()),
                    new OffsetAndMetadata(record.offset() + 1)
            );
        }
        commitOffsets();
    }

    private void commitOffsets() {
        try {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.warn("Не удалось асинхронно зафиксировать смещения: {}", exception.getMessage(), exception);
                } else {
                    log.trace("Смещения успешно зафиксированы асинхронно.");
                }
            });
            currentOffsets.clear();
        } catch (Exception e) {
            log.error("Ошибка при фиксации смещений: {}", e.getMessage(), e);
        }
    }

    public void shutdown() {
        log.info("Завершение работы обработчика событий: фиксация смещений и закрытие consumer...");
        try {
            consumer.commitSync(currentOffsets);
            consumer.close();
            log.info("Consumer успешно закрыт.");
        } catch (Exception e) {
            log.warn("Ошибка при завершении работы consumer: {}", e.getMessage(), e);
        }
    }
}
