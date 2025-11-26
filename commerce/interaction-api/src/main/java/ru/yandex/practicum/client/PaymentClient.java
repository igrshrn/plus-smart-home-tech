package ru.yandex.practicum.client;

import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.payment.PaymentDto;

import java.math.BigDecimal;
import java.util.UUID;

@Validated
@FeignClient(name = "payment", path = "/api/v1/payment")
public interface PaymentClient {
    @PostMapping
    PaymentDto payment(@RequestBody @NotNull OrderDto order);

    @PostMapping("/totalCost")
    BigDecimal getTotalCost(@RequestBody @NotNull OrderDto order);

    @PostMapping("/refund")
    void paymentSuccess(@RequestBody @NotNull UUID orderId);

    @PostMapping("/productCost")
    BigDecimal productCost(@RequestBody @NotNull OrderDto order);

    @PostMapping("/failed")
    void paymentFailed(@RequestBody @NotNull UUID orderId);
}
