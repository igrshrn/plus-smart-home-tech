package ru.yandex.practicum.dto.shoping_cart;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeProductQuantityRequest {
    @NotNull
    private UUID productId;

    @NotNull
    private long newQuantity;
}
