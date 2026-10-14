package ru.qacourse.m09.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;

/** Проверка задания. Не изменяйте этот файл. */
class ProductSchemaTestCheck {

    @Test
    @DisplayName("Проверки схемы проходят на исправном приложении")
    void passes() {
        BugCatcher.assertPassesOnCleanApp(ru.qacourse.m09.tasks.ProductSchemaTest.class);
    }

    @Test
    @DisplayName("Схема ловит ошибку контракта (проверка на испорченной схеме)")
    void schemaIsUsed() throws Exception {
        String source = java.nio.file.Files.readString(java.nio.file.Path.of(
                "src/test/java/ru/qacourse/m09/tasks/ProductSchemaTest.java"));
        org.assertj.core.api.Assertions.assertThat(source)
                .as("тест должен использовать matchesJsonSchemaInClasspath(\"schemas/product.json\")")
                .contains("matchesJsonSchemaInClasspath").contains("schemas/product.json");
    }
}
