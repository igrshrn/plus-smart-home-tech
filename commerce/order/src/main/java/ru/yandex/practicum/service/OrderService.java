package ru.yandex.practicum.service;

import ru.yandex.practicum.model.Order;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface OrderService {
    List<Order> getClientOrders(String userName);

    Order getOrderById(UUID orderId);

    Order createNewOrder(Order order);

    Order returnProducts(UUID orderId);

    Order confirmPayment(UUID orderId);

    Order failPayment(UUID orderId);

    Order setDelivery(UUID orderId, UUID deliveryId);

    Order deliverOrder(UUID orderId);

    Order failDelivery(UUID orderId);

    Order completeOrder(UUID orderId);

    Order setTotalPrice(UUID orderId, BigDecimal totalCost);

    Order setDeliveryPrice(UUID orderId, BigDecimal deliveryCost);

    Order assembleOrder(UUID orderId);

    Order failAssembly(UUID orderId);

    Order savePaymentInfo(Order order);

    public Order updateOrderWithPaymentInfo(UUID orderId, BigDecimal productPrice, BigDecimal deliveryPrice, BigDecimal totalPrice, UUID paymentId);
}
