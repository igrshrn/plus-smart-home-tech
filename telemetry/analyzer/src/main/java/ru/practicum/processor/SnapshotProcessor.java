package ru.practicum.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;
import ru.practicum.config.KafkaPropertiesConfig;
import ru.practicum.model.Scenario;
import ru.practicum.service.AnalyzerService;
import ru.practicum.service.HubEventService;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.List;

@Component
@Slf4j
public class SnapshotProcessor extends KafkaEventProcessor<SensorsSnapshotAvro> {

    private final AnalyzerService analyzerService;
    private final HubEventService hubEventService;

    public SnapshotProcessor(
            Consumer<String, SensorsSnapshotAvro> consumer,
            KafkaPropertiesConfig kafkaConfig,
            AnalyzerService analyzerService,
            HubEventService hubEventService
    ) {
        super(
                consumer,
                kafkaConfig.getConsumers().getSnapshots().getTopics(),
                kafkaConfig.getConsumerPollTimeout()
        );
        this.analyzerService = analyzerService;
        this.hubEventService = hubEventService;
    }

    @Override
    protected void handleRecord(ConsumerRecord<String, SensorsSnapshotAvro> record) throws Exception {
        Thread.sleep(100);
        log.info("Обработка снимка состояния сенсоров: ключ={}, топик={}, партиция={}, оффсет={}",
                record.key(), record.topic(), record.partition(), record.offset());

        var snapshot = record.value();
        log.debug("Анализ сценариев для хаба: {}", snapshot.getHubId());
        List<Scenario> scenarios = analyzerService.getScenariosBySnapshot(snapshot);
        log.info("Найдено {} сценариев для выполнения", scenarios.size());

        for (Scenario scenario : scenarios) {
            log.debug("Выполняем действия для сценария: {}", scenario.getName());
            hubEventService.sendActionsByScenario(scenario);
        }
        log.info("Все действия по сценариям отправлены");
    }
}