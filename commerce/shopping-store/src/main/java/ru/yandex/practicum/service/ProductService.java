package ru.yandex.practicum.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.dto.shoping_store.ProductDto;
import ru.yandex.practicum.dto.shoping_store.enums.ProductCategory;
import ru.yandex.practicum.model.Product;
import ru.yandex.practicum.model.SetProductQuantityStateRequest;

import java.util.UUID;

public interface ProductService {

    Page<ProductDto> getProductsByCategory(ProductCategory category, Pageable pageable);

    ProductDto getProductById(UUID productId);

    Product getById(UUID productId);

    ProductDto createProduct(ProductDto productDto);

    ProductDto updateProduct(ProductDto productDto);

    boolean removeProductFromStore(UUID productId);

    void updateProductQuantityState(SetProductQuantityStateRequest request);
}
