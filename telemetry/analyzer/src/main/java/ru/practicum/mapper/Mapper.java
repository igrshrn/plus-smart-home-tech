package ru.practicum.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.model.Action;
import ru.practicum.model.Condition;
import ru.practicum.model.Scenario;
import ru.practicum.model.Sensor;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.model.hub.enums.ActionType;
import ru.yandex.practicum.model.hub.enums.ConditionOperation;
import ru.yandex.practicum.model.hub.enums.ConditionType;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Mapper {

    public static Sensor mapToSensor(HubEventAvro hubEventAvro, DeviceAddedEventAvro deviceAddedEventAvro) {
        Sensor sensor = new Sensor();
        sensor.setId(deviceAddedEventAvro.getId());
        sensor.setHubId(hubEventAvro.getHubId());

        return sensor;
    }

    public static Scenario mapToScenario(HubEventAvro hubEventAvro, ScenarioAddedEventAvro scenarioAddedEventAvro) {
        Scenario scenario = new Scenario();
        scenario.setHubId(hubEventAvro.getHubId());
        scenario.setName(scenarioAddedEventAvro.getName());
        return scenario;
    }

    public static Condition mapToCondition(ScenarioConditionAvro conditionAvro) {
        Condition condition = new Condition();
        condition.setType(toConditionType(conditionAvro.getType()));
        condition.setOperation(toConditionOperation(conditionAvro.getOperation()));
        condition.setValue(getConditionValue(conditionAvro.getValue()));
        return condition;
    }

    public static Action mapToAction(DeviceActionAvro deviceActionAvro) {
        Action action = new Action();
        action.setType(toActionType(deviceActionAvro.getType()));
        action.setValue(deviceActionAvro.getValue());
        System.out.println("deviceActionAvro.getValue()");
        System.out.println(deviceActionAvro.getValue());
        System.out.println("deviceActionAvro.getValue()");
        return action;
    }

    public static ConditionType toConditionType(ConditionTypeAvro conditionTypeAvro) {
        return ConditionType.valueOf(conditionTypeAvro.name());
    }

    public static ConditionOperation toConditionOperation(ConditionOperationAvro conditionOperationAvro) {
        return ConditionOperation.valueOf(conditionOperationAvro.name());
    }

    public static ActionType toActionType(ActionTypeAvro actionTypeAvro) {
        return ActionType.valueOf(actionTypeAvro.name());
    }

    public static Integer getConditionValue(Object conditionValue) {
        return switch (conditionValue) {
            case null -> null;
            case Boolean b -> b ? 1 : 0;
            case Integer i -> i;
            default -> throw new ClassCastException("Ошибка преобразования: %s".formatted(conditionValue.getClass()));
        };
    }
}