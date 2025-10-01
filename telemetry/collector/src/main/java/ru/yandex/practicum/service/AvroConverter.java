package ru.yandex.practicum.service;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.model.hub.*;
import ru.yandex.practicum.model.sensor.*;

import java.util.List;

@Component
public class AvroConverter {

    public SensorEventAvro toSensorAvro(SensorEvent event) {
        SensorEventAvro.Builder builder = SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp().toEpochMilli());

        builder.setPayload(createSensorPayload(event));
        return builder.build();
    }

    private Object createSensorPayload(SensorEvent event) {
        if (event instanceof LightSensorEvent e) {
            return createLightSensorPayload(e);
        } else if (event instanceof MotionSensorEvent e) {
            return createMotionSensorPayload(e);
        } else if (event instanceof SwitchSensorEvent e) {
            return createSwitchSensorPayload(e);
        } else if (event instanceof ClimateSensorEvent e) {
            return createClimateSensorPayload(e);
        } else if (event instanceof TemperatureSensorEvent e) {
            return createTemperatureSensorPayload(e);
        }
        throw new IllegalArgumentException("Unknown sensor event type: " + event.getClass());
    }

    private LightSensorAvro createLightSensorPayload(LightSensorEvent e) {
        return LightSensorAvro.newBuilder()
                .setLinkQuality(e.getLinkQuality())
                .setLuminosity(e.getLuminosity())
                .build();
    }

    private MotionSensorAvro createMotionSensorPayload(MotionSensorEvent e) {
        return MotionSensorAvro.newBuilder()
                .setLinkQuality(e.getLinkQuality())
                .setMotion(e.getMotion())
                .setVoltage(e.getVoltage())
                .build();
    }

    private SwitchSensorAvro createSwitchSensorPayload(SwitchSensorEvent e) {
        return SwitchSensorAvro.newBuilder()
                .setState(e.getState())
                .build();
    }

    private ClimateSensorAvro createClimateSensorPayload(ClimateSensorEvent e) {
        return ClimateSensorAvro.newBuilder()
                .setTemperatureC(e.getTemperatureC())
                .setHumidity(e.getHumidity())
                .setCo2Level(e.getCo2Level())
                .build();
    }

    private TemperatureSensorAvro createTemperatureSensorPayload(TemperatureSensorEvent e) {
        return TemperatureSensorAvro.newBuilder()
                .setId(e.getId())
                .setHubId(e.getHubId())
                .setTimestamp(e.getTimestamp().toEpochMilli())
                .setTemperatureC(e.getTemperatureC())
                .setTemperatureF(e.getTemperatureF())
                .build();
    }

    public HubEventAvro toHubAvro(HubEvent event) {
        HubEventAvro.Builder builder = HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp().toEpochMilli());

        builder.setPayload(createHubPayload(event));
        return builder.build();
    }

    private Object createHubPayload(HubEvent event) {
        if (event instanceof DeviceAddedEvent e) {
            return createDeviceAddedPayload(e);
        } else if (event instanceof DeviceRemovedEvent e) {
            return createDeviceRemovedPayload(e);
        } else if (event instanceof ScenarioAddedEvent e) {
            return createScenarioAddedPayload(e);
        } else if (event instanceof ScenarioRemovedEvent e) {
            return createScenarioRemovedPayload(e);
        }
        throw new IllegalArgumentException("Unknown hub event type: " + event.getClass());
    }

    private DeviceAddedEventAvro createDeviceAddedPayload(DeviceAddedEvent e) {
        return DeviceAddedEventAvro.newBuilder()
                .setId(e.getId())
                .setType(DeviceTypeAvro.valueOf(e.getDeviceType().name()))
                .build();
    }

    private DeviceRemovedEventAvro createDeviceRemovedPayload(DeviceRemovedEvent e) {
        return DeviceRemovedEventAvro.newBuilder()
                .setId(e.getId())
                .build();
    }

    private ScenarioAddedEventAvro createScenarioAddedPayload(ScenarioAddedEvent e) {
        List<ScenarioConditionAvro> conditions = e.getConditions().stream()
                .map(this::createScenarioConditionAvro)
                .toList();

        List<DeviceActionAvro> actions = e.getActions().stream()
                .map(this::createDeviceActionAvro)
                .toList();

        return ScenarioAddedEventAvro.newBuilder()
                .setName(e.getName())
                .setConditions(conditions)
                .setActions(actions)
                .build();
    }

    private ScenarioConditionAvro createScenarioConditionAvro(ScenarioCondition c) {
        return ScenarioConditionAvro.newBuilder()
                .setSensorId(c.getSensorId())
                .setType(ConditionTypeAvro.valueOf(c.getType().name()))
                .setOperation(ConditionOperationAvro.valueOf(c.getOperation().name()))
                .setValue(c.getValue() != null ? c.getValue() : null)
                .build();
    }

    private DeviceActionAvro createDeviceActionAvro(DeviceAction a) {
        return DeviceActionAvro.newBuilder()
                .setSensorId(a.getSensorId())
                .setType(ActionTypeAvro.valueOf(a.getType().name()))
                .setValue(a.getValue() != null ? a.getValue() : null)
                .build();
    }

    private ScenarioRemovedEventAvro createScenarioRemovedPayload(ScenarioRemovedEvent e) {
        return ScenarioRemovedEventAvro.newBuilder()
                .setName(e.getName())
                .build();
    }
}
