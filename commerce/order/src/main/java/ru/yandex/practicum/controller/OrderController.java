package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.client.OrderClient;
import ru.yandex.practicum.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.order.ProductReturnRequest;
import ru.yandex.practicum.service.OrderOrchestratorService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderController implements OrderClient {

    private final OrderOrchestratorService orderOrchestratorService;

    @Override
    @GetMapping
    public List<OrderDto> getClientOrders(@RequestParam(name = "username") String userName) {
        return orderOrchestratorService.getClientOrders(userName);
    }

    @Override
    @PutMapping
    public OrderDto createNewOrder(@RequestBody CreateNewOrderRequest request) {
        return orderOrchestratorService.createNewOrder(request);
    }

    @Override
    @PostMapping("/return")
    public OrderDto returnProducts(@RequestBody ProductReturnRequest request) {
        return orderOrchestratorService.returnProducts(request);
    }

    @Override
    @PostMapping("/payment")
    public OrderDto payOrder(@RequestBody UUID orderId) {
        return orderOrchestratorService.payOrder(orderId);
    }

    @Override
    @PostMapping("/payment/failed")
    public OrderDto failPayment(@RequestBody UUID orderId) {
        return orderOrchestratorService.failPayment(orderId);
    }

    @Override
    @PostMapping("/payment/success")
    public OrderDto confirmPayment(@RequestBody UUID orderId) {
        return orderOrchestratorService.confirmPayment(orderId);
    }

    @Override
    @PostMapping("/delivery")
    public OrderDto deliverOrder(@RequestBody UUID orderId) {
        return orderOrchestratorService.deliverOrder(orderId);
    }

    @Override
    @PostMapping("/delivery/failed")
    public OrderDto failDelivery(@RequestBody UUID orderId) {
        return orderOrchestratorService.failDelivery(orderId);
    }

    @Override
    @PostMapping("/completed")
    public OrderDto completeOrder(@RequestBody UUID orderId) {
        return orderOrchestratorService.completeOrder(orderId);
    }

    @Override
    @PostMapping("/calculate/total")
    public OrderDto calculateTotalPrice(@RequestBody UUID orderId) {
        return orderOrchestratorService.calculateTotalPrice(orderId);
    }

    @Override
    @PostMapping("/calculate/delivery")
    public OrderDto calculateDeliveryPrice(@RequestBody UUID orderId) {
        return orderOrchestratorService.calculateDeliveryPrice(orderId);
    }

    @Override
    @PostMapping("/assembly")
    public OrderDto assembleOrder(@RequestBody UUID orderId) {
        return orderOrchestratorService.assembleOrder(orderId);
    }

    @Override
    @PostMapping("/assembly/failed")
    public OrderDto failAssembly(@RequestBody UUID orderId) {
        return orderOrchestratorService.failAssembly(orderId);
    }

    @Override
    @GetMapping("/{orderId}")
    public OrderDto getOrderById(@PathVariable UUID orderId) {
        return orderOrchestratorService.getOrderById(orderId);
    }
}
