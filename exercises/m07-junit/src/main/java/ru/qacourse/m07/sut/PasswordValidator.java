package ru.qacourse.m07.sut;

/**
 * Проверка пароля — тестируемый класс для заданий 7.4 и 7.8.
 * Пароль корректен, если: не null, длина от 6 до 30 символов включительно, есть хотя бы одна цифра.
 */
public class PasswordValidator {

    public static boolean isValid(String password) {
        if (password == null) return false;
        int min = Mutant.on(Mutant.PASSWORD_MIN_5) ? 5 : 6;
        int max = Mutant.on(Mutant.PASSWORD_MAX_31) ? 31 : 30;
        if (password.length() < min || password.length() > max) return false;
        if (Mutant.on(Mutant.PASSWORD_NO_DIGIT_NEEDED)) return true;
        return password.chars().anyMatch(Character::isDigit);
    }
}
