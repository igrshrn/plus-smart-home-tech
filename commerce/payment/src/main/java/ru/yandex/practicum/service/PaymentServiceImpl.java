package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.client.OrderClient;
import ru.yandex.practicum.client.ShoppingStoreClient;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.payment.PaymentDto;
import ru.yandex.practicum.dto.payment.PaymentState;
import ru.yandex.practicum.dto.shoping_store.ProductDto;
import ru.yandex.practicum.exception.NoPaymentFoundException;
import ru.yandex.practicum.exception.NotEnoughInfoInOrderToCalculateException;
import ru.yandex.practicum.mapper.PaymentMapper;
import ru.yandex.practicum.model.Payment;
import ru.yandex.practicum.repository.PaymentRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private static final BigDecimal BASE_VAT_RATE = BigDecimal.valueOf(0.1);

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    private final ShoppingStoreClient shoppingStoreClient;
    private final OrderClient orderClient;

    @Override
    @Transactional
    public PaymentDto payment(OrderDto order) {
        log.info("Создание платежа для заказа: orderId = {}", order.getOrderId());

        Payment newPayment = getNewPayment(order);
        Payment savedPayment = paymentRepository.save(newPayment);

        log.info("Платёж успешно создан: paymentId = {}, orderId = {}", savedPayment.getPaymentId(), savedPayment.getOrderId());

        return paymentMapper.map(savedPayment);
    }

    @Override
    public BigDecimal getTotalCost(OrderDto order) {
        log.info("Расчёт полной стоимости заказа: orderId = {}", order.getOrderId());
        BigDecimal total = calcTotalCost(order);
        log.info("Полная стоимость заказа рассчитана: orderId = {}, сумма = {}", order.getOrderId(), total);

        return total;
    }

    @Override
    @Transactional
    public void paymentSuccess(UUID orderId) {
        log.info("Обработка успешной оплаты: orderId = {}", orderId);

        updatePaymentState(orderId, PaymentState.SUCCESS);
        orderClient.confirmPayment(orderId);

        log.info("Оплата подтверждена и статус заказа обновлён: orderId = {}", orderId);
    }

    @Override
    public BigDecimal productCost(OrderDto order) {
        log.info("Расчёт стоимости товаров в заказе: orderId = {}", order.getOrderId());
        BigDecimal cost = calcProductsCost(order);
        log.info("Стоимость товаров рассчитана: orderId = {}, сумма = {}", order.getOrderId(), cost);

        return cost;
    }

    @Override
    @Transactional
    public void paymentFailed(UUID orderId) {
        log.info("Обработка неудачной оплаты: orderId = {}", orderId);

        updatePaymentState(orderId, PaymentState.FAILED);
        orderClient.failPayment(orderId);

        log.info("Оплата отклонена и статус заказа обновлён: orderId = {}", orderId);
    }

    private Payment updatePaymentState(UUID orderId, PaymentState newState) {
        Payment payment = getPaymentByOrderId(orderId);

        log.debug("Обновление статуса платежа: paymentId = {}, старый статус = {}, новый статус = {}",
                payment.getPaymentId(), payment.getState(), newState);
        payment.setState(newState);

        return paymentRepository.save(payment);
    }

    private Payment getPaymentByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId).orElseThrow(
                () -> new NoPaymentFoundException("Платёж для заказа с id = %s не найден".formatted(orderId))
        );
    }

    private Payment getNewPayment(OrderDto order) {
        BigDecimal productPrice = order.getProductPrice();
        BigDecimal feeTotal = calcFeeCost(productPrice);

        return Payment.builder()
                .orderId(order.getOrderId())
                .state(PaymentState.PENDING)
                .totalPayment(order.getTotalPrice())     // уже включает всё
                .deliveryTotal(order.getDeliveryPrice())
                .feeTotal(feeTotal)
                .build();
    }

    private BigDecimal calcTotalCost(OrderDto order) {
        BigDecimal productCost = calcProductsCost(order);
        BigDecimal deliveryCost = order.getDeliveryPrice() != null ? order.getDeliveryPrice() : BigDecimal.ZERO;
        BigDecimal vat = productCost.multiply(BASE_VAT_RATE);
        BigDecimal total = productCost.add(vat).add(deliveryCost);


        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcProductsCost(OrderDto order) {
        log.debug("Получение информации о товарах для расчёта стоимости: productId = {}", order.getProducts().keySet());
        Map<UUID, ProductDto> products = shoppingStoreClient.getProductByIds(order.getProducts().keySet())
                .stream()
                .collect(Collectors.toMap(ProductDto::getProductId, Function.identity()));

        BigDecimal totalCost = BigDecimal.ZERO;

        for (Map.Entry<UUID, Integer> orderProduct : order.getProducts().entrySet()) {
            UUID productId = orderProduct.getKey();
            Integer quantity = orderProduct.getValue();

            if (!products.containsKey(productId)) {
                log.warn("Товар не найден в магазине при расчёте стоимости: productId = {}", productId);
                throw new NotEnoughInfoInOrderToCalculateException(
                        "Товар с id = %s отсутствует в каталоге и не может быть учтён в расчёте".formatted(productId)
                );
            }

            BigDecimal price = products.get(productId).getPrice();
            if (price == null) {
                log.warn("Цена товара отсутствует: productId = {}", productId);
                throw new NotEnoughInfoInOrderToCalculateException(
                        "У товара с id = %s не указана цена".formatted(productId)
                );
            }

            BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(quantity));
            totalCost = totalCost.add(itemTotal);
        }

        return totalCost.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcFeeCost(BigDecimal cost) {
        if (cost == null) {
            return BigDecimal.ZERO;
        }
        return cost.multiply(BASE_VAT_RATE).setScale(2, RoundingMode.HALF_UP);
    }
}
