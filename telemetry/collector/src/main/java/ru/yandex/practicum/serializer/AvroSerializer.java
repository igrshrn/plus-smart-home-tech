package ru.yandex.practicum.serializer;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
@Slf4j
public class AvroSerializer implements Serializer<SpecificRecordBase> {
    private final EncoderFactory encoderFactory = EncoderFactory.get();

    @Override
    public byte[] serialize(String topic, SpecificRecordBase data) {
        if (data == null) {
            return null;
        }

        log.debug("Сериализация Avro данных для топика: {}", topic);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            BinaryEncoder encoder = encoderFactory.binaryEncoder(outputStream, null);
            DatumWriter<SpecificRecordBase> writer = new SpecificDatumWriter<>(data.getSchema());
            writer.write(data, encoder);
            encoder.flush();

            byte[] result = outputStream.toByteArray();
            log.debug("Успешно сериализовано {} байт для топика: {}", result.length, topic);
            return result;

        } catch (IOException e) {
            log.error("Ошибка сериализации Avro данных для топика: {}", topic, e);
            throw new SerializationException("Не удалось сериализовать Avro данные для топика: " + topic, e);
        }
    }
}
