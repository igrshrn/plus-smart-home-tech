package ru.practicum.handler.hub;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mapper.Mapper;
import ru.practicum.model.Sensor;
import ru.practicum.repository.SensorRepository;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DeviceAddedEventHandler implements HubEventHandler {
    private final SensorRepository sensorRepository;

    @Override
    public String getType() {
        return DeviceAddedEventAvro.class.getName();
    }

    @Transactional
    @Override
    public void handle(HubEventAvro hubEventAvro) {
        Sensor sensor = Mapper.mapToSensor(hubEventAvro, (DeviceAddedEventAvro) hubEventAvro.getPayload());
        if (!sensorRepository.existsByIdInAndHubId(List.of(sensor.getId()), sensor.getHubId())) {
            sensorRepository.save(sensor);
        }

    }
}
