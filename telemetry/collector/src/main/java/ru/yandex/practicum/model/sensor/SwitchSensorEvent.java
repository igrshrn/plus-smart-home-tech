package ru.yandex.practicum.model.sensor;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.model.sensor.enums.SensorEventType;

@Getter
@Setter
@ToString(callSuper = true)
public class SwitchSensorEvent extends SensorEvent {
    private Boolean state;

    @Override
    public SensorEventType getType() {
        return SensorEventType.SWITCH_SENSOR_EVENT;
    }
}