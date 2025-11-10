package ru.yandex.practicum.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.yandex.practicum.dto.shoping_store.enums.QuantityState;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SetProductQuantityStateRequest {
    private UUID productId;
    private QuantityState quantityState;
}
