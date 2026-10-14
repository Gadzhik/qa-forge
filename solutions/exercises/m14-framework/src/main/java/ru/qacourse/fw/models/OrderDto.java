package ru.qacourse.fw.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderDto(long id, String status, BigDecimal subtotal, BigDecimal discount, BigDecimal total, String promoCode) {
}
