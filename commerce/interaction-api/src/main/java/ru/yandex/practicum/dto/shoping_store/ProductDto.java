package ru.yandex.practicum.dto.shoping_store;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.yandex.practicum.dto.shoping_store.enums.ProductCategory;
import ru.yandex.practicum.dto.shoping_store.enums.ProductState;
import ru.yandex.practicum.dto.shoping_store.enums.QuantityState;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ProductDto {
    private UUID productId;
    private String productName;
    private String description;
    private String imageSrc;
    private QuantityState quantityState;
    private ProductState productState;
    private ProductCategory productCategory;
    private BigDecimal price;
}
