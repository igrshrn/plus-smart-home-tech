package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.client.WarehouseClient;
import ru.yandex.practicum.dto.shoping_cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.shoping_cart.ShoppingCartDto;
import ru.yandex.practicum.exception.CartDeactivatedException;
import ru.yandex.practicum.exception.ProductNotInCartException;
import ru.yandex.practicum.mapper.ShoppingCartMapper;
import ru.yandex.practicum.model.ShoppingCart;
import ru.yandex.practicum.model.ShoppingCartState;
import ru.yandex.practicum.repository.ShoppingCartRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final ShoppingCartRepository repository;
    private final WarehouseClient warehouseClient;
    private final ShoppingCartMapper mapper;

    @Override
    public ShoppingCartDto getCart(String username) {
        return mapper.toDto(findOrCreateCart(username));
    }

    @Override
    @Transactional
    public ShoppingCartDto addProducts(String username, Map<UUID, Long> productsToAdd) {
        ShoppingCart cart = findOrCreateCart(username);
        validateCartIsActive(cart, username);

        Map<UUID, Long> merged = new HashMap<>(cart.getProducts());
        productsToAdd.forEach((id, qty) -> merged.merge(id, qty, Long::sum));

        cart.setProducts(merged);
        repository.save(cart);

        return mapper.toDto(cart);
    }

    @Override
    @Transactional
    public ShoppingCartDto removeProducts(String username, List<UUID> productIds) {
        ShoppingCart cart = findOrCreateCart(username);
        validateCartIsActive(cart, username);

        productIds.forEach(cart.getProducts()::remove);
        repository.save(cart);

        return mapper.toDto(cart);
    }

    @Override
    @Transactional
    public ShoppingCartDto changeQuantity(String username, ChangeProductQuantityRequest request) {
        ShoppingCart cart = findOrCreateCart(username);
        validateCartIsActive(cart, username);

        if (!cart.getProducts().containsKey(request.getProductId())) {
            throw new ProductNotInCartException(request.getProductId());
        }
        cart.getProducts().put(request.getProductId(), request.getNewQuantity());
        repository.save(cart);

        return mapper.toDto(cart);
    }

    @Override
    @Transactional
    public void deactivateCart(String username) {
        ShoppingCart cart = findOrCreateCart(username);
        cart.setState(ShoppingCartState.DEACTIVATED);
        repository.save(cart);
    }

    private ShoppingCart findOrCreateCart(String username) {
        return repository.findByUsername(username).orElseGet(() -> {
            ShoppingCart cart = ShoppingCart.builder()
                    .shoppingCartId(UUID.randomUUID())
                    .username(username)
                    .products(new HashMap<>())
                    .state(ShoppingCartState.ACTIVE)
                    .build();
            return repository.save(cart);
        });
    }

    private void validateCartIsActive(ShoppingCart cart, String username) {
        if (cart.getState() == ShoppingCartState.DEACTIVATED) {
            throw new CartDeactivatedException(username);
        }
    }
}
