package ru.qacourse.shop.common;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/** Превращает исключения в JSON-ответы с правильными статусами. */
@RestControllerAdvice(basePackages = "ru.qacourse.shop")
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> api(ApiException e, HttpServletRequest req) {
        log.warn("{} {} -> {} {}", req.getMethod(), req.getRequestURI(), e.status().value(), e.getMessage());
        return build(e.status(), e.code(), e.getMessage(), req, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException e, HttpServletRequest req) {
        Map<String, String> fields = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(f -> fields.put(f.getField(), f.getDefaultMessage()));
        log.warn("{} {} -> 400 validation {}", req.getMethod(), req.getRequestURI(), fields);
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Некорректные данные запроса", req, fields);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> unreadable(Exception e, HttpServletRequest req) {
        log.warn("{} {} -> 400 {}", req.getMethod(), req.getRequestURI(), e.getMessage());
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Не удалось разобрать запрос", req, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> other(Exception e, HttpServletRequest req) {
        if (e instanceof org.springframework.web.ErrorResponse er) {
            HttpStatus status = HttpStatus.valueOf(er.getStatusCode().value());
            return build(status, status.name(), e.getMessage(), req, null);
        }
        log.error("{} {} -> 500", req.getMethod(), req.getRequestURI(), e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Внутренняя ошибка сервера", req, null);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message,
                                                HttpServletRequest req, Map<String, String> fields) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(Instant.now(), status.value(), code, message, req.getRequestURI(), fields));
    }
}
