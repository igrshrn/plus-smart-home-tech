package ru.yandex.practicum.service.comparator;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;

import java.util.Objects;

@Component
public class SwitchPayloadComparator implements PayloadComparator<SwitchSensorAvro> {

    @Override
    public boolean equals(SwitchSensorAvro a, SwitchSensorAvro b) {
        return Objects.equals(a.getState(), b.getState());
    }

    @Override
    public Class<SwitchSensorAvro> getSupportedType() {
        return SwitchSensorAvro.class;
    }
}
