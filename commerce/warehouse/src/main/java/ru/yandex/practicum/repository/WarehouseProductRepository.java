package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.model.WarehouseProduct;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface WarehouseProductRepository extends JpaRepository<WarehouseProduct, UUID> {
    List<WarehouseProduct> findByProductIdIn(Collection<UUID> productIds);
}
