package ru.yandex.practicum.exception;

public class CartDeactivatedException extends RuntimeException {
    public CartDeactivatedException(String username) {
        super("Cart for user '" + username + "' is deactivated");
    }
}
