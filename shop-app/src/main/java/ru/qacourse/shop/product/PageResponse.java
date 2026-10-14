package ru.qacourse.shop.product;

import java.util.List;

/** Страница результатов: сами элементы + сведения для пагинации. */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
}
