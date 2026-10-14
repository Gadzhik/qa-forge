package ru.qacourse.m09.models;

/** Ответ POST /api/auth/login. */
public record TokenResponse(String token, String type) {
}
