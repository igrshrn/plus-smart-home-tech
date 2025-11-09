package ru.practicum.service;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.practicum.exception.NotFoundException;
import ru.practicum.handler.hub.HubEventHandler;
import ru.practicum.model.Action;
import ru.practicum.model.Scenario;
import ru.practicum.model.ScenarioAction;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HubEventService {
    private final Map<String, HubEventHandler> hubEventHandlers;
    @GrpcClient("router")
    HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient;

    public HubEventService(Set<HubEventHandler> hubEventHandlers) {
        this.hubEventHandlers = hubEventHandlers.stream()
                .collect(Collectors.toMap(
                        HubEventHandler::getType,
                        Function.identity()
                ));
    }

    public void process(HubEventAvro hubEventAvro) {
        String type = hubEventAvro.getPayload().getClass().getName();
        HubEventHandler handler = hubEventHandlers.get(type);
        if (handler != null) {
            handler.handle(hubEventAvro);
        } else {
            throw new NotFoundException("Не найден обработчик для типа события: %s".formatted(type));
        }
    }

    public void sendActionsByScenario(Scenario scenario) {
        String hubId = scenario.getHubId();
        String scenarioName = scenario.getName();

        for (ScenarioAction sa : scenario.getActions()) {
            Action action = sa.getAction();
            String sensorId = sa.getSensor().getId();
            Instant ts = Instant.now();
            int grpcValue = action.getValue() != null ? action.getValue() : 0;

            DeviceActionProto deviceActionProto = DeviceActionProto.newBuilder()
                    .setSensorId(sensorId)
                    .setType(ActionTypeProto.valueOf(action.getType().name()))
                    .setValue(grpcValue)
                    .build();

            DeviceActionRequest request = DeviceActionRequest.newBuilder()
                    .setHubId(hubId)
                    .setScenarioName(scenarioName)
                    .setTimestamp(Timestamp.newBuilder()
                            .setSeconds(ts.getEpochSecond())
                            .setNanos(ts.getNano()))
                    .setAction(deviceActionProto)
                    .build();

            hubRouterClient.handleDeviceAction(request);
            log.debug("Отправлено действие: сенсор={}, тип={}, значение={}", sensorId, action.getType(), action.getValue());
        }
    }
}