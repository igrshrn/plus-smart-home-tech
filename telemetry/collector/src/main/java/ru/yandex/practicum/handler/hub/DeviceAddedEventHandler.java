package ru.yandex.practicum.handler.hub;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.model.hub.DeviceAddedEvent;
import ru.yandex.practicum.model.hub.enums.DeviceType;
import ru.yandex.practicum.service.EventCollectorService;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class DeviceAddedEventHandler implements HubEventHandler {
    private final EventCollectorService eventCollectorService;

    @Override
    public HubEventProto.PayloadCase getPayloadCase() {
        return HubEventProto.PayloadCase.DEVICE_ADDED;
    }

    @Override
    public void handle(HubEventProto event) {
        var added = event.getDeviceAdded();
        var domainEvent = new DeviceAddedEvent();
        domainEvent.setHubId(event.getHubId());
        domainEvent.setTimestamp(Instant.ofEpochSecond(
                event.getTimestamp().getSeconds(),
                event.getTimestamp().getNanos()
        ));
        domainEvent.setId(added.getId());
        domainEvent.setDeviceType(DeviceType.valueOf(added.getType().name()));

        eventCollectorService.collectHubEvent(domainEvent);
    }
}
