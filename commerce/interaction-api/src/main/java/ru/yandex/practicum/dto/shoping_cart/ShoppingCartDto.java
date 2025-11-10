package ru.yandex.practicum.dto.shoping_cart;

import lombok.*;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoppingCartDto {
    private UUID shoppingCartId;
    private Map<UUID, Long> products;
}
