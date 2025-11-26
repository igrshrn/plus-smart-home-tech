package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.order.enums.OrderState;
import ru.yandex.practicum.exception.NoOrderFoundException;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Сервис для взаимодействия с БД
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;

    @Override
    public List<Order> getClientOrders(String userName) {
        log.info("Запрос на получение всех заказов пользователя {}", userName);
        return orderRepository.findAllByUserName(userName);
    }

    @Override
    public Order getOrderById(UUID orderId) {
        return orderRepository.findById(orderId).orElseThrow(
                () -> new NoOrderFoundException("Заказ id = %s покупателя не найден".formatted(orderId))
        );
    }

    @Override
    @Transactional
    public Order createNewOrder(Order order) {
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order returnProducts(UUID orderId) {
        return updateOrderState(orderId, OrderState.PRODUCT_RETURNED);
    }

    @Override
    @Transactional
    public Order confirmPayment(UUID orderId) {
        return updateOrderState(orderId, OrderState.PAID);
    }

    @Override
    @Transactional
    public Order failPayment(UUID orderId) {
        return updateOrderState(orderId, OrderState.PAYMENT_FAILED);
    }

    @Override
    @Transactional
    public Order setDelivery(UUID orderId, UUID deliveryId) {
        Order order = getOrderById(orderId);
        order.setDeliveryId(deliveryId);

        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order deliverOrder(UUID orderId) {
        return updateOrderState(orderId, OrderState.DELIVERED);
    }

    @Override
    @Transactional
    public Order failDelivery(UUID orderId) {
        return updateOrderState(orderId, OrderState.DELIVERY_FAILED);
    }

    @Override
    @Transactional
    public Order completeOrder(UUID orderId) {
        return updateOrderState(orderId, OrderState.COMPLETED);
    }

    @Override
    @Transactional
    public Order setTotalPrice(UUID orderId, BigDecimal totalCost) {
        Order order = getOrderById(orderId);
        order.setTotalPrice(totalCost);

        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order setDeliveryPrice(UUID orderId, BigDecimal deliveryCost) {
        Order order = getOrderById(orderId);
        order.setDeliveryPrice(deliveryCost);

        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order assembleOrder(UUID orderId) {
        return updateOrderState(orderId, OrderState.ASSEMBLED);
    }

    @Override
    @Transactional
    public Order failAssembly(UUID orderId) {
        return updateOrderState(orderId, OrderState.ASSEMBLY_FAILED);
    }


    @Override
    @Transactional
    public Order savePaymentInfo(Order order) {
        Order oldOrder = getOrderById(order.getOrderId());

        oldOrder.setProductPrice(order.getProductPrice());
        oldOrder.setDeliveryPrice(order.getDeliveryPrice());
        oldOrder.setTotalPrice(order.getTotalPrice());
        oldOrder.setPaymentId(order.getPaymentId());
        oldOrder.setState(OrderState.ON_PAYMENT);

        return orderRepository.save(oldOrder);
    }

    @Override
    @Transactional
    public Order updateOrderWithPaymentInfo(UUID orderId, BigDecimal productPrice, BigDecimal deliveryPrice, BigDecimal totalPrice, UUID paymentId) {
        Order order = getOrderById(orderId);
        order.setProductPrice(productPrice);
        order.setDeliveryPrice(deliveryPrice);
        order.setTotalPrice(totalPrice);
        order.setPaymentId(paymentId);
        order.setState(OrderState.ON_PAYMENT);

        return orderRepository.save(order);
    }

    private Order updateOrderState(UUID orderId, OrderState state) {
        Order order = getOrderById(orderId);
        order.setState(state);

        return orderRepository.save(order);
    }

}
