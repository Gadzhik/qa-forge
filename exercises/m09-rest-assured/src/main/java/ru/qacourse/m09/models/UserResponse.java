package ru.qacourse.m09.models;


/**
 * Задание 9.3: модель ответа регистрации:
 * {"id": 3, "username": "anna", "email": "anna@test.local", "role": "USER", "createdAt": "2024-05-12T..."}
 * Сделайте record с полями id (long), username, email, role. Поле createdAt нам не нужно — разрешите неизвестные поля.
 */
public record UserResponse() {
}
