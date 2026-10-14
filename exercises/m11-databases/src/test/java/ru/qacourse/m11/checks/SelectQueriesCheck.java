package ru.qacourse.m11.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.m11.Queries;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания. Не изменяйте этот файл. */
class SelectQueriesCheck extends DbCheckBase {

    @Test
    @DisplayName("DRINKS_BY_PRICE")
    void drinks() throws Exception {
        assertThat(column(Queries.DRINKS_BY_PRICE)).containsExactly(
                "Вода без газа", "Кола 0.5", "Морс клюквенный", "Лимонад домашний", "Кофе латте");
    }

    @Test
    @DisplayName("COUNT_CHEAPER_THAN_100")
    void cheap() throws Exception {
        assertThat(str(column(Queries.COUNT_CHEAPER_THAN_100).get(0))).isEqualTo("4");
    }

    @Test
    @DisplayName("BURGERS_BY_NAME (без учёта регистра)")
    void burgers() throws Exception {
        assertThat(column(Queries.BURGERS_BY_NAME)).containsExactlyInAnyOrder(
                "Классический бургер", "Чизбургер двойной", "Чикенбургер", "Веганбургер", "Бургер Острый", "Бургер с беконом");
    }

    @Test
    @DisplayName("TOP3_EXPENSIVE")
    void top3() throws Exception {
        var rows = rows(Queries.TOP3_EXPENSIVE);
        assertThat(rows).extracting(r -> r.get(0)).containsExactly("Бургер с беконом", "Чизбургер двойной", "Бургер Острый");
        assertThat(str(rows.get(0).get(1))).isEqualTo("399");
    }
}
