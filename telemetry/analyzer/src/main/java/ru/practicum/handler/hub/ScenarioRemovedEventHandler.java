package ru.practicum.handler.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.repository.ScenarioRepository;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioRemovedEventHandler implements HubEventHandler {
    private final ScenarioRepository scenarioRepository;

    @Override
    public String getType() {
        return ScenarioRemovedEventAvro.class.getName();
    }

    @Transactional
    @Override
    public void handle(HubEventAvro hubEventAvro) {
        String name = ((ScenarioRemovedEventAvro) hubEventAvro.getPayload()).getName();
        log.info("scenario for deleting {}", name);
        scenarioRepository.deleteByHubIdAndName(hubEventAvro.getHubId(), name);
    }
}
