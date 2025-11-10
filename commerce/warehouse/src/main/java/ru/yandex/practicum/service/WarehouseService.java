package ru.yandex.practicum.service;


import ru.yandex.practicum.dto.shoping_cart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.dto.warehouse.NewProductInWarehouseRequest;

public interface WarehouseService {

    void registerNewProduct(NewProductInWarehouseRequest request);

    void addProductQuantity(AddProductToWarehouseRequest request);

    BookedProductsDto checkAvailabilityAndBook(ShoppingCartDto cart);

    AddressDto getWarehouseAddress();
}
