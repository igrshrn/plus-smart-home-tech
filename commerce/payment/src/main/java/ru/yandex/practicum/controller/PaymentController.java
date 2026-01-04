package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.payment.PaymentDto;
import ru.yandex.practicum.service.PaymentService;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController implements ru.yandex.practicum.client.PaymentClient {
    private final PaymentService paymentService;

    @Override
    @PostMapping
    public PaymentDto payment(@RequestBody OrderDto order) {
        return paymentService.payment(order);
    }

    @Override
    @PostMapping("/totalCost")
    public BigDecimal getTotalCost(@RequestBody OrderDto order) {
        return paymentService.getTotalCost(order);
    }

    @Override
    @PostMapping("/refund")
    public void paymentSuccess(@RequestBody UUID orderId) {
        paymentService.paymentSuccess(orderId);
    }

    @Override
    @PostMapping("/productCost")
    public BigDecimal productCost(@RequestBody OrderDto order) {
        return paymentService.productCost(order);
    }

    @Override
    @PostMapping("/failed")
    public void paymentFailed(@RequestBody UUID orderId) {
        paymentService.paymentFailed(orderId);
    }
}
