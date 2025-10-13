package ru.yandex.practicum.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.config.KafkaPropertiesConfig;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SensorEventConsumerRunner {

    private final SensorEventHandler eventHandler;
    private final KafkaConsumer<String, SensorEventAvro> consumer;
    private final KafkaPropertiesConfig kafkaProperties;

    public void start() {
        String sensorTopic = kafkaProperties.getTopics().getSensors();
        var pollTimeout = kafkaProperties.getConsumerPollTimeout();

        log.info("Запуск потребителя. Подписка на топик: {}, таймаут: {}", sensorTopic, pollTimeout);
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            consumer.subscribe(List.of(sensorTopic));
            while (true) {
                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(pollTimeout);
                eventHandler.handle(records);
            }
        } catch (WakeupException ignored) {
        } finally {
            eventHandler.shutdown();
            log.info("Потребитель остановлен.");
        }
    }
}