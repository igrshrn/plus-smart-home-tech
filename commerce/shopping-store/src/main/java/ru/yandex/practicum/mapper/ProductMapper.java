package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import ru.yandex.practicum.dto.shoping_store.ProductDto;
import ru.yandex.practicum.model.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductDto toDto(Product product);

    Product toEntity(ProductDto dto);
}
