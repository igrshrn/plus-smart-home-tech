package ru.yandex.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.shoping_store.ProductDto;
import ru.yandex.practicum.dto.shoping_store.enums.ProductCategory;

import java.util.UUID;

@FeignClient(name = "shopping-store", path = "/api/v1/shopping-store")
public interface ShoppingStoreClient {

    @GetMapping
    Page<ProductDto> getProducts(@RequestParam ProductCategory category, Pageable pageable);

    @PutMapping
    ProductDto createNewProduct(@RequestBody ProductDto dto);

    @PostMapping
    ProductDto updateProduct(@RequestBody ProductDto dto);

    @PostMapping("/removeProductFromStore")
    boolean removeProduct(@RequestBody UUID productId);

    @PostMapping("/quantityState")
    void setProductQuantityState(@RequestParam UUID productId,
                                 @RequestParam String quantityState);

    @GetMapping("/{productId}")
    ProductDto getProduct(@PathVariable UUID productId);
}