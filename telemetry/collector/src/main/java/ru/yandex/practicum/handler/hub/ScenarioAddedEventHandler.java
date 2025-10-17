package ru.yandex.practicum.handler.hub;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto;
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
                .map(this::toDomainCondition)
                .toList();
        domainEvent.setConditions(conditions);

        List<DeviceAction> actions = scenario.getActionList().stream()
                .map(this::toDomainAction)
                .toList();
        domainEvent.setActions(actions);

        eventCollectorService.collectHubEvent(domainEvent);
    }

    private ScenarioCondition toDomainCondition(ScenarioConditionProto condition) {
        var c = new ScenarioCondition();
        c.setSensorId(condition.getSensorId());
        c.setType(ConditionType.valueOf(condition.getType().name()));
        c.setOperation(ConditionOperation.valueOf(condition.getOperation().name()));

        if (condition.hasBoolValue()) {
            c.setValue(condition.getBoolValue());
        } else if (condition.hasIntValue()) {
            c.setValue(condition.getIntValue());
        }

        return c;
    }

    private DeviceAction toDomainAction(DeviceActionProto action) {
        var a = new DeviceAction();
        a.setSensorId(action.getSensorId());
        a.setType(ActionType.valueOf(action.getType().name()));

        if (action.hasValue()) {
            a.setValue(action.getValue());
        }
        return a;
    }
}
