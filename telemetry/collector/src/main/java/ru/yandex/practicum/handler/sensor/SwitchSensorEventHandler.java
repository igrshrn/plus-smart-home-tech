package ru.yandex.practicum.handler.sensor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.service.EventCollectorService;

@Component
@RequiredArgsConstructor
public class SwitchSensorEventHandler implements SensorEventHandler {
    private final EventCollectorService eventCollectorService;

    @Override
    public SensorEventProto.PayloadCase getPayloadCase() {
        return SensorEventProto.PayloadCase.SWITCH_SENSOR_EVENT;
    }

    @Override
    public void handle(SensorEventProto event) {

    }
}
