package ru.yandex.practicum.handler.sensor;

import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.handler.EventHandler;

public interface SensorEventHandler extends EventHandler<SensorEventProto> {
    @Override
    SensorEventProto.PayloadCase getPayloadCase();
}