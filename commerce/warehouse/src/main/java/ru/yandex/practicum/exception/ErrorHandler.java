package ru.yandex.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.exception.model.ErrorResponse;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleSpecifiedProductAlreadyInWarehouseException(
            final SpecifiedProductAlreadyInWarehouseException e) {
        log.warn("Attempt to add product already present in warehouse: {}", e.getMessage(), e);
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, "Bad Request", e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleNoSpecifiedProductInWarehouseException(
            final NoSpecifiedProductInWarehouseException e) {
        log.warn("Requested product not found in warehouse: {}", e.getMessage(), e);
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, "Bad Request", e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleProductInShoppingCartLowQuantityInWarehouse(
            final ProductInShoppingCartLowQuantityInWarehouse e) {
        log.warn("Not enough product quantity in warehouse for shopping cart: {}", e.getMessage(), e);
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, "Bad Request", e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleNotFoundException(
            final NotFoundException e) {
        log.warn("Resource not found: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(HttpStatus.NOT_FOUND, "Not Found", e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleProductAlreadyExistsException(ProductAlreadyExistsException e) {
        log.warn("Попытка регистрации уже существующего товара: {}", e.getMessage(), e);
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, "Bad Request", e.getMessage()));
    }
}
