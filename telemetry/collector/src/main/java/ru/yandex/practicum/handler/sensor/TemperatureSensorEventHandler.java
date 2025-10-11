package ru.yandex.practicum.handler.sensor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.model.sensor.TemperatureSensorEvent;
import ru.yandex.practicum.service.EventCollectorService;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class TemperatureSensorEventHandler implements SensorEventHandler {
    private final EventCollectorService eventCollectorService;

    @Override
    public SensorEventProto.PayloadCase getPayloadCase() {
        return SensorEventProto.PayloadCase.TEMPERATURE_SENSOR_EVENT;
    }

    @Override
    public void handle(SensorEventProto event) {
        var temp = event.getTemperatureSensorEvent();
        var domainEvent = new TemperatureSensorEvent();
        domainEvent.setId(event.getId());
        domainEvent.setHubId(event.getHubId());
        domainEvent.setTimestamp(Instant.ofEpochSecond(
                event.getTimestamp().getSeconds(),
                event.getTimestamp().getNanos()
        ));
        domainEvent.setTemperatureC(temp.getTemperatureC());
        domainEvent.setTemperatureF(temp.getTemperatureF());

        eventCollectorService.collectSensorEvent(domainEvent);
    }
}
