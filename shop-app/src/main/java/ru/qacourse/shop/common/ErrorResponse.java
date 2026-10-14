package ru.qacourse.shop.common;

import java.time.Instant;
import java.util.Map;

/** Единый формат ошибки во всех ответах API. */
public record ErrorResponse(Instant timestamp, int status, String code, String message, String path,
                            Map<String, String> fields) {
}
