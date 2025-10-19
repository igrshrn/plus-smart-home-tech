package ru.yandex.practicum.handler;

public interface EventHandler<T> {
    Object getPayloadCase();

    void handle(T event);
}
