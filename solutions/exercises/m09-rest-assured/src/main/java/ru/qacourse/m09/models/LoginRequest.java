package ru.qacourse.m09.models;

/** Тело POST /api/auth/login. */
public record LoginRequest(String username, String password) {
}
