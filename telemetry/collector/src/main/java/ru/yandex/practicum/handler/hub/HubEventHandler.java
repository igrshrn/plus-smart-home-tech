package ru.yandex.practicum.handler.hub;

import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.handler.EventHandler;

public interface HubEventHandler extends EventHandler<HubEventProto> {
    @Override
    HubEventProto.PayloadCase getPayloadCase();
}
