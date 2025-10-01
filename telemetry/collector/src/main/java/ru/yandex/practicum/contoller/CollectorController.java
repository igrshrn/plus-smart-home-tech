package ru.yandex.practicum.contoller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.model.hub.HubEvent;
import ru.yandex.practicum.model.sensor.SensorEvent;
import ru.yandex.practicum.service.EventCollectorService;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class CollectorController {
    private final EventCollectorService eventCollectorService;

    @PostMapping("/sensors")
    public ResponseEntity<Void> collectSensorEvent(@Valid @RequestBody SensorEvent event) {
        eventCollectorService.collectSensorEvent(event);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/hubs")
    public ResponseEntity<Void> collectHubEvent(@Valid @RequestBody HubEvent event) {
        eventCollectorService.collectHubEvent(event);
        return ResponseEntity.ok().build();
    }
}
