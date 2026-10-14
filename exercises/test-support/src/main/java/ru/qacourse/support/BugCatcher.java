package ru.qacourse.support;

import static org.assertj.core.api.Assertions.fail;

/**
 * Главная проверка "охоты на баги":
 * тест студента должен ПРОХОДИТЬ на исправном приложении и ПАДАТЬ, когда баг включён.
 * Только так можно быть уверенным, что тест действительно ловит дефект.
 */
public final class BugCatcher {
    private BugCatcher() {
    }

    public static void assertCatches(Class<?> studentTest, String bugCode) {
        assertCatches(studentTest, bugCode, java.util.Map.of());
    }

    /** То же, но приложение запускается с дополнительными настройками (например, недоступной "платёжкой"). */
    public static void assertCatches(Class<?> studentTest, String bugCode, java.util.Map<String, Object> props) {
        TestRunResult clean = StudentTests.runAgainstShop(studentTest, "", props);
        if (clean.found() == 0) {
            fail("В классе " + studentTest.getSimpleName() + " не найдено ни одного теста. Есть ли аннотация @Test?");
        }
        if (!clean.allPassed()) {
            fail("""
                    Тест падает на ИСПРАВНОМ приложении, значит он проверяет что-то не то (или сам содержит ошибку).
                    %s""".formatted(clean.describe()));
        }
        TestRunResult buggy = StudentTests.runAgainstShop(studentTest, bugCode, props);
        if (!buggy.anyFailed()) {
            fail("""
                    Тест проходит, даже когда баг %s включён — значит он этот баг не ловит.
                    Подумайте, какое ожидаемое поведение нарушается, и проверьте именно его.
                    %s""".formatted(bugCode, buggy.describe()));
        }
    }

    /** Тест должен проходить на исправном приложении (для заданий без конкретного бага). */
    public static void assertPassesOnCleanApp(Class<?> studentTest) {
        TestRunResult clean = StudentTests.runAgainstShop(studentTest, "");
        if (clean.found() == 0) {
            fail("В классе " + studentTest.getSimpleName() + " не найдено ни одного теста. Есть ли аннотация @Test?");
        }
        if (!clean.allPassed()) {
            fail("Тесты должны проходить на исправном приложении.\n" + clean.describe());
        }
    }
}
