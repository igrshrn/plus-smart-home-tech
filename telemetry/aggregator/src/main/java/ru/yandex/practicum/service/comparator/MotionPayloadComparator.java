package ru.yandex.practicum.service.comparator;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;

import java.util.Objects;

@Component
public class MotionPayloadComparator implements PayloadComparator<MotionSensorAvro> {

    @Override
    public boolean equals(MotionSensorAvro a, MotionSensorAvro b) {
        return Objects.equals(a.getMotion(), b.getMotion()) &&
                Objects.equals(a.getLinkQuality(), b.getLinkQuality()) &&
                Objects.equals(a.getVoltage(), b.getVoltage());
    }

    @Override
    public Class<MotionSensorAvro> getSupportedType() {
        return MotionSensorAvro.class;
    }
}
