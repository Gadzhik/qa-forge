package ru.qacourse.m07.checks;

import ru.qacourse.m07.sut.Mutant;
import ru.qacourse.support.StudentTests;
import ru.qacourse.support.TestRunResult;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.fail;

/** Запускает тесты студента на правильном коде и с каждым мутантом. */
final class MutationCheck {
    private MutationCheck() {
    }

    static void assertKills(Class<?> studentTest, Mutant... mutants) {
        Mutant.reset();
        TestRunResult clean = StudentTests.run(studentTest);
        if (clean.found() == 0) {
            fail("В классе " + studentTest.getSimpleName() + " не найдено ни одного теста. Есть ли аннотация @Test?");
        }
        if (!clean.allPassed()) {
            fail("Тесты падают на ПРАВИЛЬНОМ коде — значит, ошибка в тесте или в ожиданиях.\n" + clean.describe());
        }
        List<String> survived = new ArrayList<>();
        try {
            for (Mutant m : mutants) {
                Mutant.activate(m);
                if (!StudentTests.run(studentTest).anyFailed()) {
                    survived.add(m.name());
                }
            }
        } finally {
            Mutant.reset();
        }
        if (!survived.isEmpty()) {
            fail("Тесты НЕ поймали заложенные ошибки: " + survived
                    + "\nПодумайте, какие требования из комментария к классу вы ещё не проверили.");
        }
    }
}
