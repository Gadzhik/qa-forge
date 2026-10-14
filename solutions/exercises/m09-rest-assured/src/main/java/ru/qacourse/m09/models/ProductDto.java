package ru.qacourse.m09.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/** Товар из ответа API. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductDto(long id, String name, String description, BigDecimal price, String category, int stock, String emoji) {
}
