package ru.yandex.practicum.handler.sensor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.model.sensor.ClimateSensorEvent;
import ru.yandex.practicum.service.EventCollectorService;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ClimateSensorEventHandler implements SensorEventHandler {

    private final EventCollectorService eventCollectorService;

    @Override
    public SensorEventProto.PayloadCase getPayloadCase() {
        return SensorEventProto.PayloadCase.CLIMATE_SENSOR_EVENT;
    }

    @Override
    public void handle(SensorEventProto event) {
        var climate = event.getClimateSensorEvent();
        var domainEvent = new ClimateSensorEvent();
        domainEvent.setId(event.getId());
        domainEvent.setHubId(event.getHubId());
        domainEvent.setTimestamp(Instant.ofEpochSecond(
                event.getTimestamp().getSeconds(),
                event.getTimestamp().getNanos()
        ));
        domainEvent.setTemperatureC(climate.getTemperatureC());
        domainEvent.setHumidity(climate.getHumidity());
        domainEvent.setCo2Level(climate.getCo2Level());

        eventCollectorService.collectSensorEvent(domainEvent);
    }
}
