package ru.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.exception.NotFoundException;
import ru.practicum.handler.sensor.SensorEventHandler;
import ru.practicum.model.Condition;
import ru.practicum.model.Scenario;
import ru.practicum.model.ScenarioCondition;
import ru.practicum.repository.ScenarioRepository;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AnalyzerService {
    private final ScenarioRepository scenarioRepository;
    private final Map<String, SensorEventHandler> sensorEventHandlers;

    public AnalyzerService(ScenarioRepository scenarioRepository,
                           Set<SensorEventHandler> sensorEventHandlers) {
        this.scenarioRepository = scenarioRepository;
        this.sensorEventHandlers = sensorEventHandlers.stream()
                .collect(Collectors.toMap(
                        SensorEventHandler::getType,
                        Function.identity()
                ));
        log.info("Сервис анализа инициализирован с {} обработчиками сенсоров", sensorEventHandlers.size());
    }

    public List<Scenario> getScenariosBySnapshot(SensorsSnapshotAvro sensorsSnapshotAvro) {
        List<Scenario> scenarios = scenarioRepository.findByHubId(sensorsSnapshotAvro.getHubId());
        Map<String, SensorStateAvro> sensorStates = sensorsSnapshotAvro.getSensorsState();
        log.info("Найдено {} сценариев для хаба {}", scenarios.size(), sensorsSnapshotAvro.getHubId());

        return scenarios.stream()
                .filter(scenario -> checkConditions(scenario.getConditions(), sensorStates))
                .toList();
    }

    private boolean checkConditions(Set<ScenarioCondition> scenarioConditions, Map<String, SensorStateAvro> sensorStates) {
        return scenarioConditions.stream()
                .allMatch(sc -> checkScenarioCondition(sc, sensorStates));
    }

    private boolean checkScenarioCondition(ScenarioCondition sc, Map<String, SensorStateAvro> sensorStates) {
        String sensorId = sc.getSensor().getId();
        SensorStateAvro sensorState = sensorStates.get(sensorId);
        if (sensorState == null) {
            log.warn("Состояние сенсора не найдено для sensorId: {}", sensorId);
            return false;
        }

        Condition condition = sc.getCondition();
        String dataType = sensorState.getData().getClass().getName();
        SensorEventHandler handler = sensorEventHandlers.get(dataType);
        if (handler == null) {
            throw new NotFoundException("Не найден обработчик для типа данных сенсора: %s".formatted(dataType));
        }

        Integer sensorValue = handler.getSensorValue(condition.getType(), sensorState);
        if (sensorValue == null) {
            log.debug("Значение сенсора отсутствует для условия: {}", condition);
            return false;
        }

        Integer conditionValue = condition.getValue();
        boolean matches = switch (condition.getOperation()) {
            case LOWER_THAN -> sensorValue < conditionValue;
            case EQUALS -> sensorValue.equals(conditionValue);
            case GREATER_THAN -> sensorValue > conditionValue;
        };

        log.debug("Проверка условия: значение сенсора={} {} порог={} → {}",
                sensorValue, condition.getOperation(), conditionValue, matches);

        return matches;
    }
}