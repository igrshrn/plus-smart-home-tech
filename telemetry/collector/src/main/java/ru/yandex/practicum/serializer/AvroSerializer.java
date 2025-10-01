package ru.yandex.practicum.serializer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.io.IOException;

@Component
@Slf4j
public class AvroSerializer {
    public byte[] serialize(SensorEventAvro event) {
        try {
            return event.toByteBuffer().array();
        } catch (IOException e) {
            log.error("Ошибка сериализации SensorEventAvro в byte[]", e);
            throw new RuntimeException("Не удалось сериализовать SensorEventAvro", e);
        }
    }

    public byte[] serialize(HubEventAvro event) {
        try {
            return event.toByteBuffer().array();
        } catch (IOException e) {
            log.error("Ошибка сериализации HubEventAvro в byte[]", e);
            throw new RuntimeException("Не удалось сериализовать HubEventAvro", e);
        }
    }
}
