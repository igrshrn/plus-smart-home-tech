package ru.practicum.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class KafkaEventProcessor<T> implements Runnable {

    protected final Consumer<String, T> consumer;
    protected final List<String> topics;
    protected final Duration pollTimeout;
    protected final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    protected KafkaEventProcessor(Consumer<String, T> consumer, List<String> topics, Duration pollTimeout) {
        this.consumer = consumer;
        this.topics = topics;
        this.pollTimeout = pollTimeout;
    }

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
        try {
            consumer.subscribe(topics);
            log.info("Подписались на топики: {}", topics);

            int recordCount = 0;
            while (true) {
                ConsumerRecords<String, T> records = consumer.poll(pollTimeout);
                for (ConsumerRecord<String, T> record : records) {
                    handleRecord(record);
                    manageOffsets(record, recordCount++);
                }
            }
        } catch (WakeupException e) {
            log.info("Получен сигнал завершения — выходим из цикла опроса Kafka");
        } catch (Exception e) {
            log.error("Ошибка при обработке сообщений из Kafka", e);
        } finally {
            try {
                consumer.commitSync(currentOffsets);
                log.info("Синхронный коммит оффсетов завершён");
            } catch (Exception e) {
                log.warn("Ошибка при синхронном коммите оффсетов", e);
            } finally {
                log.info("Закрываем Kafka consumer");
                consumer.close();
            }
        }
    }

    protected abstract void handleRecord(ConsumerRecord<String, T> record) throws Exception;

    private void manageOffsets(ConsumerRecord<String, T> record, int count) {
        TopicPartition partition = new TopicPartition(record.topic(), record.partition());
        currentOffsets.put(partition, new OffsetAndMetadata(record.offset() + 1));

        if (count > 0 && count % 10 == 0) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.warn("Ошибка при асинхронном коммите оффсетов: {}", offsets, exception);
                } else {
                    log.debug("Асинхронный коммит оффсетов: {}", offsets);
                }
            });
        }
    }
}