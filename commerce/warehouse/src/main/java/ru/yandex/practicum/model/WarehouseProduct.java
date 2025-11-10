package ru.yandex.practicum.model;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseProduct {

    @Id
    private UUID productId;

    @Embedded
    private Dimension dimension;

    private double weight;
    private boolean fragile;
    private long quantity;
}
