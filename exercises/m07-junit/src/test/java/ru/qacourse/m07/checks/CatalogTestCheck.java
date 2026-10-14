package ru.qacourse.m07.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.m07.sut.Mutant;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания. Не изменяйте этот файл. */
class CatalogTestCheck {

    @Test
    @DisplayName("CatalogTest ловит три ошибки поиска")
    void kills() {
        MutationCheck.assertKills(ru.qacourse.m07.tasks.CatalogTest.class,
                Mutant.SEARCH_CASE_SENSITIVE, Mutant.SORT_ASCENDING, Mutant.SEARCH_LOSES_LAST);
    }
}
