package ru.yandex.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.service.comparator.PayloadComparator;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;

@Slf4j
@Service
public class AggregationService {

    private final Map<String, SensorsSnapshotAvro> snapshotsByHubId;

    private final Map<Class<?>, BiPredicate<Object, Object>> payloadComparators;

    public AggregationService(List<PayloadComparator<?>> comparators) {
        this.snapshotsByHubId = new ConcurrentHashMap<>();
        this.payloadComparators = new HashMap<>();

        for (PayloadComparator<?> comp : comparators) {
            payloadComparators.put(
                    comp.getSupportedType(),
                    (a, b) -> {
                        @SuppressWarnings("unchecked")
                        PayloadComparator<Object> unsafeComp = (PayloadComparator<Object>) comp;
                        return unsafeComp.equals(a, b);
                    }
            );
        }
    }

    public Optional<SensorsSnapshotAvro> aggregateEvent(SensorEventAvro event) {
        String hubId = event.getHubId();
        String sensorId = event.getId();
        Instant eventTimestamp = event.getTimestamp();
        long eventTimestampMillis = eventTimestamp.toEpochMilli();

        SensorsSnapshotAvro currentSnapshot = snapshotsByHubId.computeIfAbsent(hubId, id -> {
            log.debug("Создан новый снапшот для хаба: {}", id);
            return SensorsSnapshotAvro.newBuilder()
                    .setHubId(id)
                    .setTimestamp(eventTimestamp)
                    .setSensorsState(new HashMap<>())
                    .build();
        });

        Map<String, SensorStateAvro> sensorsState = new HashMap<>(currentSnapshot.getSensorsState());
        SensorStateAvro existingState = sensorsState.get(sensorId);

        if (existingState != null) {
            long existingTimestampMillis = existingState.getTimestamp().toEpochMilli();

            if (eventTimestampMillis < existingTimestampMillis) {
                log.debug("Событие проигнорировано: устаревший timestamp ({} < {})", eventTimestampMillis, existingTimestampMillis);
                return Optional.empty();
            }

            if (eventTimestampMillis == existingTimestampMillis) {
                if (payloadsEqual(existingState.getData(), event.getPayload())) {
                    log.debug("Событие проигнорировано: состояние не изменилось (sensorId={})", sensorId);
                    return Optional.empty();
                }
            }
        }

        SensorStateAvro newState = SensorStateAvro.newBuilder()
                .setTimestamp(eventTimestamp)
                .setData(event.getPayload())
                .build();

        sensorsState.put(sensorId, newState);

        SensorsSnapshotAvro updatedSnapshot = SensorsSnapshotAvro.newBuilder()
                .setHubId(hubId)
                .setTimestamp(eventTimestamp)
                .setSensorsState(sensorsState)
                .build();

        snapshotsByHubId.put(hubId, updatedSnapshot);
        log.debug("Обновлён снапшот для хаба: {}", hubId);

        return Optional.of(updatedSnapshot);
    }

    private boolean payloadsEqual(Object a, Object b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        if (!a.getClass().equals(b.getClass())) return false;

        BiPredicate<Object, Object> comparator = payloadComparators.get(a.getClass());
        if (comparator == null) {
            log.warn("Неизвестный тип payload: {}", a.getClass());
            return false;
        }
        return comparator.test(a, b);
    }

}