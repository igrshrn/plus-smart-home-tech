package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.client.OrderClient;
import ru.yandex.practicum.client.WarehouseClient;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.delivery.enums.DeliveryState;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.exception.NoDeliveryFoundException;
import ru.yandex.practicum.mapper.AddressMapper;
import ru.yandex.practicum.mapper.DeliveryMapper;
import ru.yandex.practicum.model.Address;
import ru.yandex.practicum.model.Delivery;
import ru.yandex.practicum.repository.AddressRepository;
import ru.yandex.practicum.repository.DeliveryRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryService {
    private static final double WEIGHT_RATE = 0.3;
    private static final double VOLUME_RATE = 0.2;
    private static final double BASE_DELIVERY_PRICE = 5.0;
    private static final double BASE_ADDRESS_COEF = 1;
    private static final double ADDRESS_1_ADDRESS_COEF = 1;
    private static final double ADDRESS_2_ADDRESS_COEF = 2;
    private static final double DIFF_STREET_ADDRESS_COEF = 1.2;
    private static final double FRAGILE_COEF = 1.2;

    private final DeliveryRepository deliveryRepository;
    private final AddressRepository addressRepository;
    private final DeliveryMapper deliveryMapper;
    private final AddressMapper addressMapper;
    private final OrderClient orderClient;
    private final WarehouseClient warehouseClient;

    @Transactional
    public DeliveryDto createDelivery(DeliveryDto deliveryDto) {
        log.info("Создание доставки для заказа id = {}", deliveryDto.getOrderId());

        Address savedFrom = getOrCreateAddress(deliveryDto.getFromAddress());
        Address savedTo = getOrCreateAddress(deliveryDto.getToAddress());

        Delivery delivery = Delivery.builder()
                .orderId(deliveryDto.getOrderId())
                .fromAddress(savedFrom)
                .toAddress(savedTo)
                .deliveryState(DeliveryState.CREATED)
                .build();

        Delivery savedDelivery = deliveryRepository.save(delivery);
        log.info("Доставка для заказа id = {} создана, id доставки = {}",
                deliveryDto.getOrderId(), savedDelivery.getDeliveryId());

        return deliveryMapper.toDto(savedDelivery);
    }

    @Transactional
    protected Address getOrCreateAddress(AddressDto addressDto) {
        Address address = addressMapper.toEntity(addressDto);

        return addressRepository.findByCountryAndCityAndStreetAndHouseAndFlat(
                address.getCountry(),
                address.getCity(),
                address.getStreet(),
                address.getHouse(),
                address.getFlat()
        ).orElseGet(() -> addressRepository.save(address));
    }

    @Transactional
    public void markAsSuccessful(UUID orderId) {
        log.info("Запрос на смену статуса(DELIVERED) доставки с id = {}", orderId);
        Delivery delivery = findDeliveryByOrderId(orderId);
        delivery.setDeliveryState(DeliveryState.DELIVERED);
        deliveryRepository.save(delivery);
        log.info("Доставка с id = {} успешно доставлена", orderId);
    }

    @Transactional
    public void markAsPicked(UUID orderId) {
        log.info("Запрос на смену статуса(IN_PROGRESS) доставки с id = {}", orderId);
        Delivery delivery = findDeliveryByOrderId(orderId);
        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);
        deliveryRepository.save(delivery);

        warehouseClient.shippedToDelivery(deliveryMapper.toDeliveryRequest(delivery));
        log.info("Приняты товары в доставку id = {} ", orderId);
    }

    @Transactional
    public void markAsFailed(UUID orderId) {
        Delivery delivery = findDeliveryByOrderId(orderId);

        deliveryRepository.save(delivery);
        delivery.setDeliveryState(DeliveryState.FAILED);

        orderClient.failDelivery(orderId);

        log.info("Доставка не была вручена клиенут id = {}", orderId);
    }

    public BigDecimal calculateCost(OrderDto orderDto) {
        Delivery delivery = findDeliveryByOrderId(orderDto.getOrderId());

        log.info("Рассчет стоимости для доставки: {}", delivery);

        // Базовая стоимость
        BigDecimal cost = BigDecimal.valueOf(BASE_DELIVERY_PRICE);

        // Коэффициент от адреса отправителя
        BigDecimal fromAddressCoef = getCoefFromAddress(delivery.getFromAddress());
        cost = cost.add(BigDecimal.valueOf(BASE_DELIVERY_PRICE).multiply(fromAddressCoef));

        // Стоимость за хрупкость
        BigDecimal fragileCoef = getFragileCoefficient(orderDto.isFragile());
        cost = cost.multiply(fragileCoef);

        // Стоимость за вес
        BigDecimal weightCost = BigDecimal.valueOf(orderDto.getDeliveryWeight())
                .multiply(BigDecimal.valueOf(WEIGHT_RATE));
        cost = cost.add(weightCost);

        // Стоимость за объем
        BigDecimal volumeCost = BigDecimal.valueOf(orderDto.getDeliveryVolume())
                .multiply(BigDecimal.valueOf(VOLUME_RATE));
        cost = cost.add(volumeCost);

        BigDecimal distanceCoef = getCoefByToAddress(delivery.getFromAddress(), delivery.getToAddress());
        cost = cost.multiply(distanceCoef);

        return cost.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal getCoefFromAddress(Address address) {
        String addressStr = address.toString();
        if (addressStr.contains("ADDRESS_1")) {
            return BigDecimal.valueOf(ADDRESS_1_ADDRESS_COEF);
        } else if (addressStr.contains("ADDRESS_2")) {
            return BigDecimal.valueOf(ADDRESS_2_ADDRESS_COEF);
        } else {
            return BigDecimal.valueOf(BASE_ADDRESS_COEF);
        }
    }

    private BigDecimal getCoefByToAddress(Address from, Address to) {
        if (!from.getStreet().equals(to.getStreet())) {
            return BigDecimal.valueOf(DIFF_STREET_ADDRESS_COEF);
        }

        return BigDecimal.valueOf(1.0);
    }

    private BigDecimal getFragileCoefficient(boolean isFragile) {
        double val = isFragile ? FRAGILE_COEF : 1.0;
        return BigDecimal.valueOf(val);
    }

    private Delivery findDeliveryByOrderId(UUID orderId) {
        return deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoDeliveryFoundException("Доставка с ID = %s не найдена".formatted(orderId)));
    }

}
