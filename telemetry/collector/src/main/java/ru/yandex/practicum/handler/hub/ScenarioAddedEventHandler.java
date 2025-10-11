package ru.yandex.practicum.handler.hub;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.model.hub.DeviceAction;
import ru.yandex.practicum.model.hub.ScenarioAddedEvent;
import ru.yandex.practicum.model.hub.ScenarioCondition;
import ru.yandex.practicum.model.hub.enums.ActionType;
import ru.yandex.practicum.model.hub.enums.ConditionOperation;
import ru.yandex.practicum.model.hub.enums.ConditionType;
import ru.yandex.practicum.service.EventCollectorService;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ScenarioAddedEventHandler implements HubEventHandler {
    private final EventCollectorService eventCollectorService;

    @Override
    public HubEventProto.PayloadCase getPayloadCase() {
        return HubEventProto.PayloadCase.SCENARIO_ADDED;
    }

    @Override
    public void handle(HubEventProto event) {
        var scenario = event.getScenarioAdded();
        var domainEvent = new ScenarioAddedEvent();
        domainEvent.setHubId(event.getHubId());
        domainEvent.setTimestamp(Instant.ofEpochSecond(
                event.getTimestamp().getSeconds(),
                event.getTimestamp().getNanos()
        ));
        domainEvent.setName(scenario.getName());

        List<ScenarioCondition> conditions = scenario.getConditionList().stream()
                .map(cond -> {
                    var c = new ScenarioCondition();
                    c.setSensorId(cond.getSensorId());
                    c.setType(ConditionType.valueOf(cond.getType().name()));
                    c.setOperation(ConditionOperation.valueOf(cond.getOperation().name()));
                    if (cond.hasIntValue()) {
                        c.setValue(cond.getIntValue());
                    }
                    return c;
                }).toList();
        domainEvent.setConditions(conditions);

        List<DeviceAction> actions = scenario.getActionList().stream()
                .map(act -> {
                    var a = new DeviceAction();
                    a.setSensorId(act.getSensorId());
                    a.setType(ActionType.valueOf(act.getType().name()));
                    if (act.hasValue()) {
                        a.setValue(act.getValue());
                    }
                    return a;
                }).toList();
        domainEvent.setActions(actions);

        eventCollectorService.collectHubEvent(domainEvent);
    }
}
