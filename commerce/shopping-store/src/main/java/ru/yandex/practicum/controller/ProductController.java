package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.client.ShoppingStoreClient;
import ru.yandex.practicum.dto.shoping_store.ProductDto;
import ru.yandex.practicum.dto.shoping_store.enums.ProductCategory;
import ru.yandex.practicum.dto.shoping_store.enums.QuantityState;
import ru.yandex.practicum.model.SetProductQuantityStateRequest;
import ru.yandex.practicum.service.ProductService;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shopping-store")
public class ProductController implements ShoppingStoreClient {

    private final ProductService service;

    @GetMapping
    public Page<ProductDto> getProducts(@RequestParam ProductCategory category, Pageable pageable) {
        log.info("GET /api/v1/shopping-store?category={} page={}", category, pageable);
        return service.getProductsByCategory(category, pageable);
    }

    @PutMapping
    public ProductDto createNewProduct(@RequestBody @Valid ProductDto dto) {
        log.info("PUT /api/v1/shopping-store body={}", dto);
        return service.createProduct(dto);
    }

    @PostMapping
    public ProductDto updateProduct(@RequestBody ProductDto dto) {
        log.info("POST /api/v1/shopping-store body={}", dto);
        return service.updateProduct(dto);
    }

    @PostMapping("/removeProductFromStore")
    public boolean removeProduct(@RequestBody UUID productId) {
        log.info("POST /api/v1/shopping-store/removeProductFromStore id={}", productId);
        return service.removeProductFromStore(productId);
    }

    @PostMapping("/quantityState")
    public void setProductQuantityState(@RequestParam UUID productId,
                                        @RequestParam String quantityState) {
        service.updateProductQuantityState(
                new SetProductQuantityStateRequest(productId, QuantityState.valueOf(quantityState))
        );
    }

    @GetMapping("/{productId}")
    public ProductDto getProduct(@PathVariable UUID productId) {
        log.info("GET /api/v1/shopping-store/{}", productId);
        return service.getProductById(productId);
    }
}