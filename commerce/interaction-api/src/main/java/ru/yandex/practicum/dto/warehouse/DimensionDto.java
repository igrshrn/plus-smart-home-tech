package ru.yandex.practicum.dto.warehouse;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DimensionDto {
    private double width;
    private double height;
    private double depth;
}