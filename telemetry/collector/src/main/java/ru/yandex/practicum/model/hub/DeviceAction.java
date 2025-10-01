package ru.yandex.practicum.model.hub;

import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.model.hub.enums.ActionType;

@Getter
@Setter
public class DeviceAction {
    private String sensorId;
    private ActionType type;
    private Integer value;
}