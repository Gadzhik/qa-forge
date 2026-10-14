package ru.qacourse.fw.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CartDto(List<Line> items, int totalQuantity, BigDecimal total) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Line(long productId, String name, BigDecimal price, int quantity, BigDecimal lineTotal) {
    }
}
