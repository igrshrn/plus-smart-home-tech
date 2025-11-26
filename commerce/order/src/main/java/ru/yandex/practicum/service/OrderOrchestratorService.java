package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.order.ProductReturnRequest;

import java.util.List;
import java.util.UUID;

public interface OrderOrchestratorService {
    List<OrderDto> getClientOrders(String userName);

    OrderDto createNewOrder(CreateNewOrderRequest request);

    OrderDto returnProducts(ProductReturnRequest request);

    OrderDto payOrder(UUID orderId);

    OrderDto confirmPayment(UUID orderId);

    OrderDto failPayment(UUID orderId);

    OrderDto deliverOrder(UUID orderId);

    OrderDto failDelivery(UUID orderId);

    OrderDto completeOrder(UUID orderId);

    OrderDto calculateTotalPrice(UUID orderId);

    OrderDto calculateDeliveryPrice(UUID orderId);

    OrderDto assembleOrder(UUID orderId);

    OrderDto failAssembly(UUID orderId);

    OrderDto getOrderById(UUID orderId);
}
