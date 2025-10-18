package ru.practicum.handler.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.model.hub.enums.ConditionType;

@Component
public class ClimateSensorEventHandler implements SensorEventHandler {
    @Override
    public String getType() {
        return ClimateSensorAvro.class.getName();
    }

    @Override
    public Integer getSensorValue(ConditionType type, SensorStateAvro sensorStateAvro) {
        ClimateSensorAvro climateSensorAvro = (ClimateSensorAvro) sensorStateAvro.getData();
        return switch (type) {
            case ConditionType.TEMPERATURE -> climateSensorAvro.getTemperatureC();
            case ConditionType.CO2LEVEL -> climateSensorAvro.getCo2Level();
            case ConditionType.HUMIDITY -> climateSensorAvro.getHumidity();
            default -> null;
        };
    }
}
