package ru.yandex.practicum.dto.shoping_cart;

import jakarta.validation.constraints.NotEmpty;
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

    @NotEmpty
    private Map<UUID, Long> products;
}
