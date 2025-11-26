package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.shoping_store.ProductDto;
import ru.yandex.practicum.dto.shoping_store.enums.ProductCategory;
import ru.yandex.practicum.dto.shoping_store.enums.ProductState;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.mapper.ProductMapper;
import ru.yandex.practicum.model.Product;
import ru.yandex.practicum.model.SetProductQuantityStateRequest;
import ru.yandex.practicum.repository.ProductRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;
    private final ProductMapper mapper;

    @Override
    public Page<ProductDto> getProductsByCategory(ProductCategory category, Pageable pageable) {
        return repository.findAllByProductCategory(category, pageable)
                .map(mapper::toDto);
    }

    @Override
    public ProductDto getProductById(UUID productId) {
        return repository.findById(productId)
                .map(mapper::toDto)
                .orElseThrow(() -> new NotFoundException("Товар с ID = %s не найден".formatted(productId)));
    }

    @Override
    public Product getById(UUID productId) {
        return repository.findById(productId)
                .orElseThrow(() -> new NotFoundException(
                        "Товар с ID = %s не найден".formatted(productId)));
    }

    @Override
    @Transactional
    public ProductDto createProduct(ProductDto productDto) {
        Product product = mapper.toEntity(productDto);

        if (product.getProductState() == null) {
            product.setProductState(ProductState.ACTIVE);
        }

        return mapper.toDto(repository.save(product));
    }

    @Override
    @Transactional
    public ProductDto updateProduct(ProductDto productDto) {
        UUID id = productDto.getProductId();
        getById(id);
        Product updated = repository.save(mapper.toEntity(productDto));

        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean removeProductFromStore(UUID productId) {
        Product product = getById(productId);
        product.setProductState(ProductState.DEACTIVATE);

        return true;
    }

    @Override
    @Transactional
    public void updateProductQuantityState(SetProductQuantityStateRequest request) {
        Product product = getById(request.getProductId());
        product.setQuantityState(request.getQuantityState());
    }

    @Override
    public List<ProductDto> getProductsByIds(List<UUID> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        List<Product> products = repository.findAllByIdIn(productIds);
        return products.stream()
                .map(mapper::toDto)
                .toList();
    }
}
