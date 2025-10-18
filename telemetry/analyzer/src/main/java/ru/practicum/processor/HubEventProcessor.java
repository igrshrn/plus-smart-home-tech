package ru.practicum.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;
import ru.practicum.config.KafkaPropertiesConfig;
import ru.practicum.service.HubEventService;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;


@Component
@Slf4j
public class HubEventProcessor extends KafkaEventProcessor<HubEventAvro> {

    private final HubEventService hubEventService;

    public HubEventProcessor(
            Consumer<String, HubEventAvro> consumer,
            KafkaPropertiesConfig kafkaConfig,
            HubEventService hubEventService
    ) {
        super(
                consumer,
                kafkaConfig.getConsumers().getHubs().getTopics(),
                kafkaConfig.getConsumerPollTimeout()
        );
        this.hubEventService = hubEventService;
    }

    @Override
    protected void handleRecord(ConsumerRecord<String, HubEventAvro> record) throws Exception {
        log.info("Обработка события хаба: ключ={}, топик={}, партиция={}, оффсет={}",
                record.key(), record.topic(), record.partition(), record.offset());
        hubEventService.process(record.value());
        log.debug("Событие хаба успешно обработано");
    }
}