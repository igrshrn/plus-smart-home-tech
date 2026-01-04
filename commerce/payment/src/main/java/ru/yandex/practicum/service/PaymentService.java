package ru.yandex.practicum.service;


import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.payment.PaymentDto;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentService {

    PaymentDto payment(OrderDto order);

    BigDecimal getTotalCost(OrderDto order);

    void paymentSuccess(UUID orderId);

    BigDecimal productCost(OrderDto order);

    void paymentFailed(UUID orderId);
}
