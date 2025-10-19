package ru.yandex.practicum.grpc;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc.CollectorControllerImplBase;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.handler.EventHandler;
import ru.yandex.practicum.handler.hub.HubEventHandler;
import ru.yandex.practicum.handler.sensor.SensorEventHandler;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@GrpcService
@RequiredArgsConstructor
public class EventGrpcService extends CollectorControllerImplBase {
    private final Map<SensorEventProto.PayloadCase, SensorEventHandler> sensorEventHandlers;
    private final Map<HubEventProto.PayloadCase, HubEventHandler> hubEventHandlers;

    @Autowired
    public EventGrpcService(Set<SensorEventHandler> sensorEventHandlerSet, Set<HubEventHandler> hubEventHandlerSet) {
        this.sensorEventHandlers = sensorEventHandlerSet.stream()
                .collect(Collectors.toMap(
                        SensorEventHandler::getPayloadCase, Function.identity()
                ));
        this.hubEventHandlers = hubEventHandlerSet.stream().
                collect(Collectors.toMap(
                        HubEventHandler::getPayloadCase, Function.identity()
                ));
    }

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        processEvent(request, responseObserver, sensorEventHandlers, SensorEventProto::getPayloadCase);
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        processEvent(request, responseObserver, hubEventHandlers, HubEventProto::getPayloadCase);
    }

    private <T, K> void processEvent(
            T request,
            StreamObserver<Empty> responseObserver,
            Map<K, ? extends EventHandler<T>> handlers,
            Function<T, K> payloadCaseExtractor
    ) {
        try {
            K payloadCase = payloadCaseExtractor.apply(request);
            if (handlers.containsKey(payloadCase)) {
                handlers.get(payloadCase).handle(request);
            } else {
                throw new IllegalArgumentException("Не могу найти обработчик для события " + payloadCase);
            }
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(Status.fromThrowable(e)));
        }
    }
}
