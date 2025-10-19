package ru.yandex.practicum.service.comparator;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;

import java.util.Objects;

@Component
public class ClimatePayloadComparator implements PayloadComparator<ClimateSensorAvro> {

    @Override
    public boolean equals(ClimateSensorAvro a, ClimateSensorAvro b) {
        return Objects.equals(a.getTemperatureC(), b.getTemperatureC()) &&
                Objects.equals(a.getHumidity(), b.getHumidity()) &&
                Objects.equals(a.getCo2Level(), b.getCo2Level());
    }

    @Override
    public Class<ClimateSensorAvro> getSupportedType() {
        return ClimateSensorAvro.class;
    }
}