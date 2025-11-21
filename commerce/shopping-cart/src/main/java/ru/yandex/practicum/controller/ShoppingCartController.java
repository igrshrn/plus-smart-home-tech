package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.client.ShoppingCartClient;
import ru.yandex.practicum.dto.shoping_cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.shoping_cart.ShoppingCartDto;
import ru.yandex.practicum.service.ShoppingCartService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shopping-cart")
public class ShoppingCartController implements ShoppingCartClient {

    private final ShoppingCartService service;

    @GetMapping
    public ShoppingCartDto getCart(@RequestParam String username) {
        log.info("GET /api/v1/shopping-cart username={}", username);
        return service.getCart(username);
    }

    @PutMapping
    public ShoppingCartDto addProduct(@RequestParam String username,
                                      @RequestBody Map<UUID, Long> productsToAdd) {
        log.info("PUT /api/v1/shopping-cart username={} products={}", username, productsToAdd);
        return service.addProducts(username, productsToAdd);
    }

    @PostMapping("/remove")
    public ShoppingCartDto removeProducts(@RequestParam String username,
                                          @RequestBody List<UUID> productIds) {
        log.info("POST /api/v1/shopping-cart/remove username={} productIds={}", username, productIds);
        return service.removeProducts(username, productIds);
    }

    @PostMapping("/change-quantity")
    public ShoppingCartDto changeQuantity(@RequestParam String username,
                                          @RequestBody ChangeProductQuantityRequest request) {
        log.info("POST /api/v1/shopping-cart/change-quantity username={} request={}", username, request);
        return service.changeQuantity(username, request);
    }

    @DeleteMapping
    public void deactivateCart(@RequestParam String username) {
        log.info("DELETE /api/v1/shopping-cart username={}", username);
        service.deactivateCart(username);
    }
}
