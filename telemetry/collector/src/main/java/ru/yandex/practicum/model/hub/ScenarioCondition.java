package ru.yandex.practicum.model.hub;

import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.model.hub.enums.ConditionOperation;
import ru.yandex.practicum.model.hub.enums.ConditionType;

@Getter
@Setter
public class ScenarioCondition {
    private String sensorId;
    private ConditionType type;
    private ConditionOperation operation;
    private Integer value;
}
