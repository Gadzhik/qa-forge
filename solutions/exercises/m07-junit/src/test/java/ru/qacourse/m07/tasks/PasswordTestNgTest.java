package ru.qacourse.m07.tasks;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.qacourse.m07.sut.PasswordValidator;

import static org.testng.Assert.assertEquals;

/**
 * Задание 7.8: те же проверки пароля, но на TestNG с @DataProvider.
 * Один тестовый метод, который получает (пароль, ожидаемый результат) из провайдера данных.
 * Метод пометьте группой "regression": @Test(dataProvider = "...", groups = "regression").
 */
public class PasswordTestNgTest {

    @DataProvider(name = "passwords")
    public Object[][] passwords() {
        return new Object[][]{
                {"abcde1", true},
                {"abcd1", false},
                {"123456789012345678901234567890", true},
                {"1234567890123456789012345678901", false},
                {"abcdefgh", false},
                {null, false}
        };
    }

    @Test(dataProvider = "passwords", groups = "regression")
    public void validation(String password, boolean expected) {
        assertEquals(PasswordValidator.isValid(password), expected, "пароль: " + password);
    }
}
