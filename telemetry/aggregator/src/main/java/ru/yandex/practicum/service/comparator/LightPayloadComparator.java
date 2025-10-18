package ru.yandex.practicum.service.comparator;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;

import java.util.Objects;

@Component
public class LightPayloadComparator implements PayloadComparator<LightSensorAvro> {

    @Override
    public boolean equals(LightSensorAvro a, LightSensorAvro b) {
        return Objects.equals(a.getLuminosity(), b.getLuminosity()) &&
                Objects.equals(a.getLinkQuality(), b.getLinkQuality());
    }

    @Override
    public Class<LightSensorAvro> getSupportedType() {
        return LightSensorAvro.class;
    }
}
