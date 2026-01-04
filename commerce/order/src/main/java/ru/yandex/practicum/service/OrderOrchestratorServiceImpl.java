package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.client.DeliveryClient;
import ru.yandex.practicum.client.PaymentClient;
import ru.yandex.practicum.client.ShoppingCartClient;
import ru.yandex.practicum.client.WarehouseClient;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.delivery.enums.DeliveryState;
import ru.yandex.practicum.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.order.ProductReturnRequest;
import ru.yandex.practicum.dto.order.enums.OrderState;
import ru.yandex.practicum.dto.payment.PaymentDto;
import ru.yandex.practicum.dto.shoping_cart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.mapper.OrderMapper;
import ru.yandex.practicum.model.Order;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderOrchestratorServiceImpl implements OrderOrchestratorService {
    private final OrderService orderService;
    private final OrderMapper orderMapper;

    private final ShoppingCartClient shoppingCartClient;
    private final PaymentClient paymentClient;
    private final DeliveryClient deliveryClient;
    private final WarehouseClient warehouseClient;

    @Override
    public List<OrderDto> getClientOrders(String userName) {
        return orderService.getClientOrders(userName)
                .stream()
                .map(orderMapper::map)
                .toList();
    }

    @Override
    public OrderDto createNewOrder(CreateNewOrderRequest request) {
        Order order = orderService.createNewOrder(getNewOrderFromRequest(request));
        UUID deliveryId = getNewDeliveryId(order.getOrderId(), request.getDeliveryAddress());

        return orderMapper.map(orderService.setDelivery(order.getOrderId(), deliveryId));
    }

    @Override
    public OrderDto returnProducts(ProductReturnRequest request) {
        warehouseClient.acceptReturn(request.getProducts());

        return orderMapper.map(orderService.returnProducts(request.getOrderId()));
    }

    @Override
    @Transactional
    public OrderDto payOrder(UUID orderId) {
        log.info("Starting payment process for order: {}", orderId);

        // 1. Получаем текущий заказ
        Order order = orderService.getOrderById(orderId);

        // 2. Получаем данные от других сервисов
        OrderDto orderDto = orderMapper.map(order);
        BigDecimal productCost = paymentClient.productCost(orderDto);
        BigDecimal deliveryCost = deliveryClient.deliveryCost(orderDto);
        BigDecimal totalCost = paymentClient.getTotalCost(orderDto);
        PaymentDto paymentDto = paymentClient.payment(orderDto);

        // 3. Атомарно обновляем заказ
        Order updatedOrder = orderService.updateOrderWithPaymentInfo(
                orderId,
                productCost,
                deliveryCost,
                totalCost,
                paymentDto.getPaymentId()
        );

        log.info("Payment processed successfully for order: {}, paymentId: {}",
                orderId, paymentDto.getPaymentId());

        return orderMapper.map(updatedOrder);
    }

    @Override
    public OrderDto confirmPayment(UUID orderId) {
        return orderMapper.map(orderService.confirmPayment(orderId));
    }

    @Override
    public OrderDto failPayment(UUID orderId) {
        return orderMapper.map(orderService.failPayment(orderId));
    }

    @Override
    public OrderDto deliverOrder(UUID orderId) {
        return orderMapper.map(orderService.deliverOrder(orderId));
    }

    @Override
    public OrderDto failDelivery(UUID orderId) {
        return orderMapper.map(orderService.failDelivery(orderId));
    }

    @Override
    public OrderDto completeOrder(UUID orderId) {
        return orderMapper.map(orderService.completeOrder(orderId));
    }

    @Override
    public OrderDto calculateTotalPrice(UUID orderId) {
        Order order = orderService.getOrderById(orderId);
        BigDecimal totalCost = paymentClient.getTotalCost(orderMapper.map(order));

        return orderMapper.map(orderService.setTotalPrice(orderId, totalCost));
    }

    @Override
    public OrderDto calculateDeliveryPrice(UUID orderId) {
        Order order = orderService.getOrderById(orderId);
        BigDecimal deliveryCost = deliveryClient.deliveryCost(orderMapper.map(order));

        return orderMapper.map(orderService.setDeliveryPrice(orderId, deliveryCost));
    }

    @Override
    public OrderDto assembleOrder(UUID orderId) {
        warehouseClient.assemblyProductsForOrder(getNewAssemblyProductsForOrderRequest(orderId));

        return orderMapper.map(orderService.assembleOrder(orderId));
    }

    @Override
    public OrderDto failAssembly(UUID orderId) {
        return orderMapper.map(orderService.failAssembly(orderId));
    }

    @Override
    public OrderDto getOrderById(UUID orderId) {
        return orderMapper.map(orderService.getOrderById(orderId));
    }

    private ProductReturnRequest getNewAssemblyProductsForOrderRequest(UUID orderId) {
        Order order = orderService.getOrderById(orderId);
        ProductReturnRequest request = new ProductReturnRequest();
        request.setOrderId(orderId);
        request.setProducts(order.getProducts());

        return request;
    }

    private Order getNewOrderFromRequest(CreateNewOrderRequest request) {
        ShoppingCartDto cart = shoppingCartClient.getCart(request.getUserName());
        BookedProductsDto bookedProductsDto = warehouseClient.checkAvailability(cart);

        return Order.builder()
                .userName(request.getUserName())
                .cartId(request.getShoppingCart().getShoppingCartId())
                .products(request.getShoppingCart().getProducts())
                .deliveryWeight(bookedProductsDto.getDeliveryWeight())
                .deliveryVolume(bookedProductsDto.getDeliveryVolume())
                .fragile(bookedProductsDto.isFragile())
                .state(OrderState.NEW)
                .build();
    }

    private UUID getNewDeliveryId(UUID orderId, AddressDto deliveryAddress) {
        DeliveryDto deliveryDto = new DeliveryDto();
        deliveryDto.setFromAddress(warehouseClient.getWarehouseAddress());
        deliveryDto.setToAddress(deliveryAddress);
        deliveryDto.setOrderId(orderId);
        deliveryDto.setDeliveryState(DeliveryState.CREATED);


        return deliveryClient.planDelivery(deliveryDto).getDeliveryId();
    }
}
