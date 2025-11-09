package ru.yandex.practicum.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.shoping_cart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.*;
import ru.yandex.practicum.model.Dimension;
import ru.yandex.practicum.model.WarehouseProduct;
import ru.yandex.practicum.repository.WarehouseProductRepository;

import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private static final String[] ADDRESSES = {"ADDRESS_1", "ADDRESS_2"};
    private static final SecureRandom RANDOM = new SecureRandom();
    private static String currentAddress;
    private final WarehouseProductRepository repository;

    @PostConstruct
    public void init() {
        int i = RANDOM.nextInt(ADDRESSES.length);
        currentAddress = ADDRESSES[i];
    }


    @Override
    public void registerNewProduct(NewProductInWarehouseRequest request) {
        if (repository.existsById(request.getProductId())) {
            throw new IllegalStateException("Product already exists on warehouse");
        }

        DimensionDto dd = request.getDimension();
        Dimension dimension = dd == null ? null : Dimension.builder()
                .width(dd.getWidth())
                .height(dd.getHeight())
                .depth(dd.getDepth())
                .build();

        WarehouseProduct product = WarehouseProduct.builder()
                .productId(request.getProductId())
                .fragile(request.isFragile())
                .dimension(dimension)
                .weight(request.getWeight())
                .quantity(0L)
                .build();

        repository.save(product);
    }

    @Override
    public void addProductQuantity(AddProductToWarehouseRequest request) {
        WarehouseProduct product = repository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalStateException("Product not found"));
        product.setQuantity(product.getQuantity() + request.getQuantity());
        repository.save(product);
    }

    @Override
    public BookedProductsDto checkAvailabilityAndBook(ShoppingCartDto cart) {
        double totalWeight = 0;
        double totalVolume = 0;
        boolean hasFragile = false;

        for (Map.Entry<UUID, Long> entry : cart.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            long quantity = entry.getValue();

            WarehouseProduct product = repository.findById(productId)
                    .orElseThrow(() -> new IllegalStateException("Product not found"));

            if (product.getQuantity() < quantity) {
                throw new IllegalStateException("Not enough quantity for product " + productId);
            }

            product.setQuantity(product.getQuantity() - quantity);
            repository.save(product);

            totalWeight += product.getWeight() * quantity;

            Dimension d = product.getDimension();
            totalVolume += (d.getDepth() * d.getHeight() * d.getWidth()) * quantity;

            if (product.isFragile()) {
                hasFragile = true;
            }
        }

        return BookedProductsDto.builder()
                .deliveryWeight(totalWeight)
                .deliveryVolume(totalVolume)
                .fragile(hasFragile)
                .build();
    }

    @Override
    public AddressDto getWarehouseAddress() {
        return new AddressDto(currentAddress, currentAddress, currentAddress, currentAddress, currentAddress);
    }
}
