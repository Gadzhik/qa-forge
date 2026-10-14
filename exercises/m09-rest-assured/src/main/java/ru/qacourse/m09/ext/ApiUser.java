package ru.qacourse.m09.ext;

/** Зарегистрированный тестовый пользователь и его токен. */
public record ApiUser(long id, String username, String password, String token) {
}
