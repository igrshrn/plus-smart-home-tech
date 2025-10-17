package ru.practicum.handler.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.Mapper;
import ru.practicum.model.*;
import ru.practicum.repository.ActionRepository;
import ru.practicum.repository.ConditionRepository;
import ru.practicum.repository.ScenarioRepository;
import ru.practicum.repository.SensorRepository;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioAddedEventHandler implements HubEventHandler {

    private final ScenarioRepository scenarioRepository;
    private final SensorRepository sensorRepository;
    private final ActionRepository actionRepository;
    private final ConditionRepository conditionRepository;

    @Override
    public String getType() {
        return ScenarioAddedEventAvro.class.getName();
    }

    @Transactional
    @Override
    public void handle(HubEventAvro hubEventAvro) {
        ScenarioAddedEventAvro payload = (ScenarioAddedEventAvro) hubEventAvro.getPayload();
        String hubId = hubEventAvro.getHubId();

        // 1. Проверяем существование всех сенсоров из условий и действий
        List<String> conditionSensorIds = getConditionsSensorIds(payload.getConditions());
        List<String> actionSensorIds = getActionsSensorIds(payload.getActions());

        if (!checkSensors(conditionSensorIds, hubId)) {
            throw new NotFoundException("Не найдены сенсоры условий сценария: %s".formatted(conditionSensorIds));
        }
        if (!checkSensors(actionSensorIds, hubId)) {
            throw new NotFoundException("Не найдены сенсоры действий сценария: %s".formatted(actionSensorIds));
        }

        // 2. Ищем существующий сценарий
        Optional<Scenario> existingScenarioOpt = scenarioRepository.findByHubIdAndName(hubId, payload.getName());

        Scenario scenario;
        String logAction;
        Set<Long> oldConditionIds = new HashSet<>();
        Set<Long> oldActionIds = new HashSet<>();

        if (existingScenarioOpt.isEmpty()) {
            // Создаём новый сценарий
            scenario = Mapper.mapToScenario(hubEventAvro, payload);
            logAction = "added";
        } else {
            // Обновляем существующий: собираем ID старых условий и действий для последующего удаления
            scenario = existingScenarioOpt.get();
            logAction = "updated";

            oldConditionIds = scenario.getConditions().stream()
                    .map(sc -> sc.getCondition().getId())
                    .collect(Collectors.toSet());

            oldActionIds = scenario.getActions().stream()
                    .map(sa -> sa.getAction().getId())
                    .collect(Collectors.toSet());

            // Очищаем старые связи (JPA удалит их благодаря orphanRemoval = true)
            scenario.getConditions().clear();
            scenario.getActions().clear();
        }

        // 3. Создаём новые условия и действия через промежуточные сущности
        List<ScenarioCondition> newConditions = new ArrayList<>();
        for (ScenarioConditionAvro condAvro : payload.getConditions()) {
            Sensor sensor = sensorRepository.findByIdAndHubId(condAvro.getSensorId(), hubId)
                    .orElseThrow(() -> new NotFoundException("Сенсор не найден после валидации: %s".formatted(condAvro.getSensorId())));

            Condition condition = Mapper.mapToCondition(condAvro);
            condition = conditionRepository.save(condition);

            ScenarioCondition sc = new ScenarioCondition();
            sc.setScenario(scenario);
            sc.setSensor(sensor);
            sc.setCondition(condition);
            newConditions.add(sc);
        }

        List<ScenarioAction> newActions = new ArrayList<>();
        for (DeviceActionAvro actionAvro : payload.getActions()) {
            Sensor sensor = sensorRepository.findByIdAndHubId(actionAvro.getSensorId(), hubId)
                    .orElseThrow(() -> new NotFoundException("Сенсор не найден после валидации"));

            Action action = Mapper.mapToAction(actionAvro);
            action = actionRepository.save(action);

            ScenarioAction sa = new ScenarioAction();
            sa.setScenario(scenario);
            sa.setSensor(sensor);
            sa.setAction(action);
            newActions.add(sa);
        }

        scenario.getConditions().addAll(newConditions);
        scenario.getActions().addAll(newActions);

        // 4. Сохраняем сценарий (все связи сохранятся автоматически благодаря каскадам)
        scenarioRepository.save(scenario);

        log.info("Сценарий {} [id={}, имя={}, hubId={}]", logAction, scenario.getId(), scenario.getName(), scenario.getHubId());

        // 5. Удаляем старые неиспользуемые Condition и Action (если был update)
        deleteUnusedConditions(oldConditionIds);
        deleteUnusedActions(oldActionIds);
    }

    private List<String> getConditionsSensorIds(Collection<ScenarioConditionAvro> conditionsAvro) {
        return conditionsAvro.stream().map(ScenarioConditionAvro::getSensorId).toList();
    }

    private List<String> getActionsSensorIds(Collection<DeviceActionAvro> actionsAvro) {
        return actionsAvro.stream().map(DeviceActionAvro::getSensorId).toList();
    }

    private boolean checkSensors(Collection<String> ids, String hubId) {
        if (ids.isEmpty()) return true;
        return sensorRepository.existsByIdInAndHubId(ids, hubId);
    }

    private void deleteUnusedConditions(Collection<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            conditionRepository.deleteAllById(ids);
        }
    }

    private void deleteUnusedActions(Collection<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            actionRepository.deleteAllById(ids);
        }
    }
}