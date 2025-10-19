package ru.yandex.practicum.service.comparator;

public interface PayloadComparator<T> {
    boolean equals(T a, T b);

    Class<T> getSupportedType();
}
