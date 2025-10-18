package ru.yandex.practicum.handler.hub;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.model.hub.DeviceRemovedEvent;
import ru.yandex.practicum.service.EventCollectorService;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class DeviceRemovedEventHandler implements HubEventHandler {
    private final EventCollectorService eventCollectorService;

    @Override
    public HubEventProto.PayloadCase getPayloadCase() {
        return HubEventProto.PayloadCase.DEVICE_REMOVED;
    }

    @Override
    public void handle(HubEventProto event) {
        var removed = event.getDeviceRemoved();
        var domainEvent = new DeviceRemovedEvent();
        domainEvent.setHubId(event.getHubId());
        domainEvent.setTimestamp(Instant.ofEpochSecond(
                event.getTimestamp().getSeconds(),
                event.getTimestamp().getNanos()
        ));
        domainEvent.setId(removed.getId());

        eventCollectorService.collectHubEvent(domainEvent);
    }
}
