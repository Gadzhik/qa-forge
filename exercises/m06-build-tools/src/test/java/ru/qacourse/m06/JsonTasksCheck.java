package ru.qacourse.m06;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания. Не изменяйте этот файл. */
class JsonTasksCheck {

    static final String COLA = """
            {"id": 12, "name": "Кола 0.5", "price": 99.0, "category": "DRINK", "in_stock": true}
            """;

    @Test
    @DisplayName("parseProduct читает поле in_stock в inStock (@JsonProperty)")
    void parse() throws Exception {
        ProductJson p = JsonTasks.parseProduct(COLA);
        assertThat(p).isEqualTo(new ProductJson(12, "Кола 0.5", 99.0, "DRINK", true));
    }

    @Test
    @DisplayName("toJson пишет in_stock и правильные значения")
    void write() throws Exception {
        String json = JsonTasks.toJson(new ProductJson(1, "Бургер", 289.9, "BURGER", false));
        assertThat(json).contains("\"in_stock\":false").contains("\"name\":\"Бургер\"").contains("\"price\":289.9");
    }

    @Test
    @DisplayName("parseProducts читает массив")
    void list() throws Exception {
        List<ProductJson> list = JsonTasks.parseProducts("[" + COLA + "," + COLA.replace("12", "13") + "]");
        assertThat(list).extracting(ProductJson::id).containsExactly(12L, 13L);
    }

    @Test
    @DisplayName("firstProductName через JsonNode")
    void tree() throws Exception {
        String page = "{\"content\":[{\"name\":\"Классический бургер\"},{\"name\":\"Кола\"}],\"totalElements\":20}";
        assertThat(JsonTasks.firstProductName(page)).isEqualTo("Классический бургер");
    }
}
