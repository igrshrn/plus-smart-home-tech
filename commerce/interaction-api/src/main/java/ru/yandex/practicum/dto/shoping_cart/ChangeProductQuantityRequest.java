package ru.yandex.practicum.dto.shoping_cart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeProductQuantityRequest {
    private UUID productId;
    private long newQuantity;
}
