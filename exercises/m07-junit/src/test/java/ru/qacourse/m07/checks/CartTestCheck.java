package ru.qacourse.m07.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.m07.sut.Mutant;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания. Не изменяйте этот файл. */
class CartTestCheck {

    @Test
    @DisplayName("Ваши тесты CartTest ловят ошибки «итог без количества» и «повтор заменяет количество»")
    void kills() {
        MutationCheck.assertKills(ru.qacourse.m07.tasks.CartTest.class,
                Mutant.TOTAL_IGNORES_QUANTITY, Mutant.SAME_ITEM_REPLACES);
    }
}
