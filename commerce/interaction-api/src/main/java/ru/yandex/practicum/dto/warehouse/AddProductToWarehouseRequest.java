package ru.yandex.practicum.dto.warehouse;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddProductToWarehouseRequest {
    private UUID productId;
    private long quantity;
}
