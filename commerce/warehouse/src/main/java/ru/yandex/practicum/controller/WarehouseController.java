package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.client.WarehouseClient;
import ru.yandex.practicum.dto.delivery.ShippedToDeliveryRequest;
import ru.yandex.practicum.dto.order.ProductReturnRequest;
import ru.yandex.practicum.dto.shoping_cart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.service.WarehouseService;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
public class WarehouseController implements WarehouseClient {

    private final WarehouseService service;

    @PutMapping
    public void registerNewProduct(@RequestBody NewProductInWarehouseRequest request) {
        log.info("Register new product on warehouse: productId={}, fragile={}, weight={}, dimension={}x{}x{}",
                request.getProductId(),
                request.isFragile(),
                request.getWeight(),
                request.getDimension() != null ? request.getDimension().getWidth() : null,
                request.getDimension() != null ? request.getDimension().getHeight() : null,
                request.getDimension() != null ? request.getDimension().getDepth() : null
        );
        service.registerNewProduct(request);
        log.info("Registered product: productId={}", request.getProductId());
    }

    @PostMapping("/add")
    public void addProductQuantity(@RequestBody AddProductToWarehouseRequest request) {
        log.info("Add product quantity: productId={}, quantity={}", request.getProductId(), request.getQuantity());
        service.addProductQuantity(request);
        log.info("Quantity added: productId={}, quantity={}", request.getProductId(), request.getQuantity());
    }

    @PostMapping("/check")
    public BookedProductsDto checkAvailability(@RequestBody ShoppingCartDto cart) {
        log.info("Check availability & book: cartId={}, itemsCount={}",
                cart.getShoppingCartId(),
                cart.getProducts() != null ? cart.getProducts().size() : 0
        );
        BookedProductsDto result = service.checkAvailabilityAndBook(cart);
        log.info("Booked: cartId={}, deliveryWeight={}, deliveryVolume={}, fragile={}",
                cart.getShoppingCartId(),
                result.getDeliveryWeight(),
                result.getDeliveryVolume(),
                result.isFragile()
        );
        return result;
    }

    @GetMapping("/address")
    public AddressDto getWarehouseAddress() {
        log.info("Get warehouse address");
        AddressDto address = service.getWarehouseAddress();
        log.info("Warehouse address resolved: country={}, city={}, street={}, house={}, flat={}",
                address.getCountry(), address.getCity(), address.getStreet(), address.getHouse(), address.getFlat());
        return address;
    }

    @Override
    @PostMapping("/shipped")
    public void shippedToDelivery(@RequestBody @Valid ShippedToDeliveryRequest request) {
        service.shippedToDelivery(request);
    }

    @Override
    @PostMapping("/return")
    public void acceptReturn(Map<UUID, Long> products) {
        service.acceptReturn(products);
    }


    @Override
    @PostMapping("/assembly")
    public BookedProductsDto assemblyProductsForOrder(ProductReturnRequest request) {
        return service.assembleProductsForOrder(request);
    }
}
