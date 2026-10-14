package ru.qacourse.m09.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Задание 9.3: модель тела POST /api/auth/register: {"username": "...", "email": "...", "password": "..."}.
 * Сделайте её через Lombok: @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
}
