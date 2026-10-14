package ru.qacourse.m09.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

/** Единый формат ошибки QA Burger. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ErrorResponse(int status, String code, String message, String path, Map<String, String> fields) {
}
