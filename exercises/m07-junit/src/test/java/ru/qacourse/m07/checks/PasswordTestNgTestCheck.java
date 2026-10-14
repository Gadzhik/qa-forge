package ru.qacourse.m07.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.m07.sut.Mutant;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания. Не изменяйте этот файл. */
class PasswordTestNgTestCheck {

    private static boolean runTestNg() {
        TestListenerAdapter listener = new TestListenerAdapter();
        TestNG testng = new TestNG(false);
        testng.setTestClasses(new Class[]{ru.qacourse.m07.tasks.PasswordTestNgTest.class});
        testng.setUseDefaultListeners(false);
        testng.setVerbose(0);
        testng.addListener(listener);
        testng.run();
        if (listener.getPassedTests().isEmpty() && listener.getFailedTests().isEmpty()) {
            throw new AssertionError("TestNG не нашёл тестов в PasswordTestNgTest");
        }
        return listener.getFailedTests().isEmpty() && listener.getSkippedTests().isEmpty();
    }

    @Test
    @DisplayName("Используется @DataProvider и группа regression")
    void structure() {
        var methods = Arrays.asList(ru.qacourse.m07.tasks.PasswordTestNgTest.class.getDeclaredMethods());
        assertThat(methods).anyMatch(m -> m.isAnnotationPresent(org.testng.annotations.DataProvider.class));
        assertThat(methods).anyMatch(m -> m.isAnnotationPresent(org.testng.annotations.Test.class)
                && !m.getAnnotation(org.testng.annotations.Test.class).dataProvider().isEmpty()
                && Arrays.asList(m.getAnnotation(org.testng.annotations.Test.class).groups()).contains("regression"));
    }

    @Test
    @DisplayName("TestNG-тест проходит на правильном коде и ловит ошибки границ")
    void kills() {
        Mutant.reset();
        assertThat(runTestNg()).as("тесты должны проходить на правильном коде").isTrue();
        try {
            for (Mutant m : new Mutant[]{Mutant.PASSWORD_MIN_5, Mutant.PASSWORD_MAX_31, Mutant.PASSWORD_NO_DIGIT_NEEDED}) {
                Mutant.activate(m);
                assertThat(runTestNg()).as("ошибка " + m + " не поймана").isFalse();
            }
        } finally {
            Mutant.reset();
        }
    }
}
