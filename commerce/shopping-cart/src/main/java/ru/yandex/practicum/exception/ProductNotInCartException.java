package ru.yandex.practicum.exception;

import java.util.UUID;

public class ProductNotInCartException extends RuntimeException {
    public ProductNotInCartException(UUID productId) {
        super("Product with ID " + productId + " is not in the cart");
    }
}
