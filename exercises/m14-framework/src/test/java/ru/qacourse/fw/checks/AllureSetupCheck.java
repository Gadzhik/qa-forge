package ru.qacourse.fw.checks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.shop.launcher.ShopAppLauncher;
import ru.qacourse.support.StudentTests;
import ru.qacourse.support.TestRunResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/** Проверка задания 14.2: шаги и метки попадают в результаты Allure. Не изменяйте. */
@org.junit.jupiter.api.parallel.Isolated   // проверки меняют общие системные свойства — запускать отдельно от всех
class AllureSetupCheck {

    @Test
    @DisplayName("OrderFlowTest проходит, в отчёте Allure есть шаги клиентов и метки epic/feature/story/severity/tms")
    void allureResults() throws Exception {
        Path dir = Path.of(System.getProperty("allure.results.directory", "target/allure-results"));
        if (Files.isDirectory(dir)) {
            try (Stream<Path> files = Files.list(dir)) {
                files.forEach(f -> f.toFile().delete());
            }
        }
        TestRunResult r;
        try (ShopAppLauncher.Running app = ShopAppLauncher.start()) {
            System.setProperty("shop.url", app.baseUrl());
            r = StudentTests.run(ru.qacourse.fw.tests.OrderFlowTest.class);
        } finally {
            System.clearProperty("shop.url");
        }
        assertThat(r.allPassed()).as(r.describe()).isTrue();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode result = null;
        try (Stream<Path> files = Files.list(dir)) {
            for (Path f : files.filter(p -> p.toString().endsWith("-result.json")).toList()) {
                JsonNode json = mapper.readTree(f.toFile());
                if (json.path("name").asText().contains("Заказ из корзины")) result = json;
            }
        }
        assertThat(result).as("результат OrderFlowTest в target/allure-results").isNotNull();

        List<String> steps = new ArrayList<>();
        collect(result.path("steps"), steps);
        assertThat(steps).as("шаги @Step из CartClient и OrderClient")
                .anyMatch(s -> s.startsWith("Добавить в корзину товар"))
                .anyMatch(s -> s.startsWith("Оформить заказ"))
                .anyMatch(s -> s.startsWith("Получить корзину"));

        List<String> labels = new ArrayList<>();
        result.path("labels").forEach(l -> labels.add(l.path("name").asText() + "=" + l.path("value").asText()));
        assertThat(labels).contains("epic=Магазин", "feature=Заказы", "story=Оформление заказа", "severity=critical");
        List<String> links = new ArrayList<>();
        result.path("links").forEach(l -> links.add(l.path("name").asText()));
        assertThat(links).contains("TC-ORDER-1");
    }

    private static void collect(JsonNode steps, List<String> out) {
        for (JsonNode s : steps) {
            out.add(s.path("name").asText());
            collect(s.path("steps"), out);
        }
    }
}
