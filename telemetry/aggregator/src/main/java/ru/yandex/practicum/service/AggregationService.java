package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AggregationService {

    private final Map<String, SensorsSnapshotAvro> snapshotsByHubId = new HashMap<>();

    public Optional<SensorsSnapshotAvro> aggregateEvent(SensorEventAvro event) {
        String hubId = event.getHubId();
        String sensorId = event.getId();
        long eventTimestampMillis = event.getTimestamp().toEpochMilli();

        SensorsSnapshotAvro currentSnapshot = snapshotsByHubId.computeIfAbsent(hubId, id -> {
            log.debug("Создан новый снапшот для хаба: {}", id);
            return SensorsSnapshotAvro.newBuilder()
                    .setHubId(id)
                    .setTimestamp(Instant.ofEpochSecond(eventTimestampMillis))
                    .setSensorsState(new HashMap<>())
                    .build();
        });

        Map<String, SensorStateAvro> sensorsState = new HashMap<>(currentSnapshot.getSensorsState());
        SensorStateAvro existingState = sensorsState.get(sensorId);

        if (existingState != null) {
            long existingTimestampMillis = existingState.getTimestamp().toEpochMilli();

            if (eventTimestampMillis <= existingTimestampMillis) {
                return Optional.empty();
            }
        }

        SensorStateAvro newState = SensorStateAvro.newBuilder()
                .setTimestamp(Instant.ofEpochMilli(eventTimestampMillis))
                .setData(event.getPayload())
                .build();

        sensorsState.put(sensorId, newState);

        SensorsSnapshotAvro updatedSnapshot = SensorsSnapshotAvro.newBuilder()
                .setHubId(hubId)
                .setTimestamp(Instant.ofEpochSecond(eventTimestampMillis))
                .setSensorsState(sensorsState)
                .build();

        snapshotsByHubId.put(hubId, updatedSnapshot);
        log.debug("Обновлён снапшот для хаба: {}", hubId);

        return Optional.of(updatedSnapshot);
    }
}