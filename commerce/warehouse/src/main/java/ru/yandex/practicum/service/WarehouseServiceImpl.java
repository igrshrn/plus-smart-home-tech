package ru.yandex.practicum.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.delivery.ShippedToDeliveryRequest;
import ru.yandex.practicum.dto.order.ProductReturnRequest;
import ru.yandex.practicum.dto.shoping_cart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.*;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.exception.ProductAlreadyExistsException;
import ru.yandex.practicum.exception.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.model.Booking;
import ru.yandex.practicum.model.Dimension;
import ru.yandex.practicum.model.WarehouseProduct;
import ru.yandex.practicum.repository.BookingRepository;
import ru.yandex.practicum.repository.WarehouseRepository;

import java.security.SecureRandom;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarehouseServiceImpl implements WarehouseService {

    private static final String[] ADDRESSES = {"ADDRESS_1", "ADDRESS_2"};
    private static final SecureRandom RANDOM = new SecureRandom();
    private static String currentAddress;
    private final WarehouseRepository warehouseRepository;
    private final BookingRepository bookingRepository;

    @PostConstruct
    public void init() {
        int i = RANDOM.nextInt(ADDRESSES.length);
        currentAddress = ADDRESSES[i];
    }


    @Override
    @Transactional
    public void registerNewProduct(NewProductInWarehouseRequest request) {
        log.info("Регистрация нового товара на складе: productId = {}", request.getProductId());
        if (warehouseRepository.existsById(request.getProductId())) {
            log.warn("Попытка повторной регистрации товара: productId = {}", request.getProductId());
            throw new ProductAlreadyExistsException("Product already exists on warehouse");
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

        warehouseRepository.save(product);
        log.info("Товар успешно зарегистрирован на складе: productId = {}", request.getProductId());
    }

    @Override
    @Transactional
    public void addProductQuantity(AddProductToWarehouseRequest request) {
        log.info("Увеличение количества товара на складе: productId = {}, количество = {}",
                request.getProductId(), request.getQuantity());

        WarehouseProduct product = warehouseRepository.findById(request.getProductId())
                .orElseThrow(() -> new NotFoundException("Товар с id = %s не найден".formatted(request.getProductId())));
        product.setQuantity(product.getQuantity() + request.getQuantity());
        warehouseRepository.save(product);

        log.info("Количество товара обновлено: productId = {}, новое количество = {}",
                request.getProductId(), product.getQuantity());
    }


    @Override
    public AddressDto getWarehouseAddress() {
        log.debug("Запрос адреса склада: текущий адрес = {}", currentAddress);

        return new AddressDto(currentAddress, currentAddress, currentAddress, currentAddress, currentAddress);
    }

    @Override
    @Transactional
    public void shippedToDelivery(ShippedToDeliveryRequest request) {
        log.info("Подтверждение передачи заказа в доставку: orderId = {}, deliveryId = {}",
                request.getOrderId(), request.getDeliveryId());

        Booking booking = getBookingById(request.getOrderId());
        booking.setDeliveryId(request.getDeliveryId());
        bookingRepository.save(booking);

        log.info("Заказ {} успешно передан в доставку с deliveryId = {}",
                request.getOrderId(), request.getDeliveryId());
    }

    @Override
    @Transactional
    public void acceptReturn(Map<UUID, Long> products) {
        Map<UUID, WarehouseProduct> warehouseProducts = getWarehouseProducts(products.keySet());
        for (Map.Entry<UUID, Long> product : products.entrySet()) {
            WarehouseProduct warehouseProduct = warehouseProducts.get(product.getKey());
            warehouseProduct.setQuantity(warehouseProduct.getQuantity() + product.getValue());
        }
        saveWarehouseRemains(warehouseProducts.values());
    }

    @Override
    @Transactional
    public BookedProductsDto checkAvailabilityAndBook(ShoppingCartDto cart) {
        log.info("Проверка наличия и бронирование товаров для корзины: {}", cart.getProducts());

        ValidationResult result = validateAndCalculateCartProducts(cart.getProducts(), false);

        BookedProductsDto response = new BookedProductsDto(result.weight(), result.volume(), result.fragile());
        log.info("Бронирование успешно завершено: вес = {}, объём = {}, хрупкий = {}",
                result.weight(), result.volume(), result.fragile());
        return response;
    }

    @Override
    @Transactional
    public BookedProductsDto assembleProductsForOrder(ProductReturnRequest request) {
        log.info("Сборка заказа: orderId = {}, товары = {}", request.getOrderId(), request.getProducts());

        ValidationResult result = validateAndCalculateCartProducts(request.getProducts(), true);

        for (Map.Entry<UUID, Long> entry : request.getProducts().entrySet()) {
            WarehouseProduct product = result.products().get(entry.getKey());
            product.setQuantity(product.getQuantity() - entry.getValue());
        }

        saveWarehouseRemains(result.products().values());

        addBooking(request);

        BookedProductsDto response = new BookedProductsDto(result.weight(), result.volume(), result.fragile());
        log.info("Сборка заказа завершена: orderId = {}, вес = {}, объём = {}, хрупкий = {}",
                request.getOrderId(), result.weight(), result.volume(), result.fragile());
        return response;
    }

    private ValidationResult validateAndCalculateCartProducts(Map<UUID, Long> cartProducts, boolean forAssembly) {
        if (cartProducts.isEmpty()) {
            return new ValidationResult(0.0, 0.0, false, Collections.emptyMap());
        }

        Map<UUID, WarehouseProduct> products = getWarehouseProducts(cartProducts.keySet());

        double weight = 0;
        double volume = 0;
        boolean fragile = false;

        for (Map.Entry<UUID, Long> entry : cartProducts.entrySet()) {
            UUID productId = entry.getKey();
            Long requestedQty = entry.getValue();
            WarehouseProduct product = products.get(productId);

            long availableQty = product.getQuantity();
            if (forAssembly) {
                if (availableQty < requestedQty) {
                    log.warn("Недостаточно товара на складе для списания: productId = {}, запрошено = {}, доступно = {}",
                            productId, requestedQty, availableQty);
                    throw new ProductInShoppingCartLowQuantityInWarehouse(
                            "Недостаточное количество товара с id = %s на складе".formatted(productId));
                }
            } else {
                if (requestedQty > availableQty) {
                    log.warn("Недостаточно товара на складе для бронирования: productId = {}, запрошено = {}, доступно = {}",
                            productId, requestedQty, availableQty);
                    throw new ProductInShoppingCartLowQuantityInWarehouse(
                            "Недостаточное количество товара с id = %s на складе".formatted(productId));
                }
            }

            weight += product.getWeight() * requestedQty;

            Dimension dim = product.getDimension();
            double productVolume = 0;
            if (dim != null) {
                productVolume = dim.getWidth() * dim.getHeight() * dim.getDepth();
            } else {
                log.debug("Габариты товара отсутствуют (dimension = null), объём считается как 0. productId = {}", productId);
            }
            volume += productVolume * requestedQty;

            fragile = fragile || product.isFragile();
        }

        return new ValidationResult(weight, volume, fragile, products);
    }

    private Booking getBookingById(UUID orderId) {
        return bookingRepository.findById(orderId).orElseThrow(
                () -> {
                    log.warn("Бронирование не найдено: orderId = {}", orderId);
                    return new NotFoundException("Нет бронирования с id = %s".formatted(orderId));
                }
        );
    }

    Map<UUID, WarehouseProduct> getWarehouseProducts(Collection<UUID> ids) {
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }

        List<WarehouseProduct> found = warehouseRepository.findAllById(ids);
        Map<UUID, WarehouseProduct> products = found.stream()
                .collect(Collectors.toMap(WarehouseProduct::getProductId, Function.identity()));

        if (products.size() != ids.size()) {
            Set<UUID> requestedIds = new HashSet<>(ids);
            Set<UUID> foundIds = products.keySet();
            requestedIds.removeAll(foundIds); // остаются только отсутствующие

            String missingIdsStr = requestedIds.stream()
                    .map(UUID::toString)
                    .collect(Collectors.joining(", "));

            log.warn("Товары отсутствуют на складе: [{}]", missingIdsStr);
            throw new ProductInShoppingCartLowQuantityInWarehouse(
                    "Товары отсутствуют на складе: [%s]".formatted(missingIdsStr)
            );
        }

        return products;
    }

    void addBooking(ProductReturnRequest request) {
        Booking booking = Booking.builder()
                .orderId(request.getOrderId())
                .products(request.getProducts())
                .build();
        bookingRepository.save(booking);
        log.debug("Создано бронирование для заказа: orderId = {}", request.getOrderId());
    }

    void saveWarehouseRemains(Collection<WarehouseProduct> products) {
        warehouseRepository.saveAll(products);
    }

    private record ValidationResult(double weight, double volume, boolean fragile,
                                    Map<UUID, WarehouseProduct> products) {
    }
}
