package ru.yandex.practicum.service.comparator;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;

import java.util.Objects;

@Component
public class TemperaturePayloadComparator implements PayloadComparator<TemperatureSensorAvro> {

    @Override
    public boolean equals(TemperatureSensorAvro a, TemperatureSensorAvro b) {
        return Objects.equals(a.getTemperatureC(), b.getTemperatureC()) &&
                Objects.equals(a.getTemperatureF(), b.getTemperatureF());
    }

    @Override
    public Class<TemperatureSensorAvro> getSupportedType() {
        return TemperatureSensorAvro.class;
    }
}
