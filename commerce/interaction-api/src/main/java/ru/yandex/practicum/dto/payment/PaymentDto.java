package ru.yandex.practicum.dto.payment;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
    @NotNull
    private UUID paymentId;

    private Double totalPayment;
    private Double deliveryTotal;
    private Double feeTotal;
}
