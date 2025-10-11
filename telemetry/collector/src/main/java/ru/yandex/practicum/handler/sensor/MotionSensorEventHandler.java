package ru.yandex.practicum.handler.sensor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.model.sensor.MotionSensorEvent;
import ru.yandex.practicum.service.EventCollectorService;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class MotionSensorEventHandler implements SensorEventHandler {
    private final EventCollectorService eventCollectorService;

    @Override
    public SensorEventProto.PayloadCase getPayloadCase() {
        return SensorEventProto.PayloadCase.MOTION_SENSOR_EVENT;
    }

    @Override
    public void handle(SensorEventProto event) {
        var motion = event.getMotionSensorEvent();
        var domainEvent = new MotionSensorEvent();
        domainEvent.setId(event.getId());
        domainEvent.setHubId(event.getHubId());
        domainEvent.setTimestamp(Instant.ofEpochSecond(
                event.getTimestamp().getSeconds(),
                event.getTimestamp().getNanos()
        ));
        domainEvent.setLinkQuality(motion.getLinkQuality());
        domainEvent.setMotion(motion.getMotion());
        domainEvent.setVoltage(motion.getVoltage());

        eventCollectorService.collectSensorEvent(domainEvent);
    }
}
