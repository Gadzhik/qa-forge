package ru.qacourse.m06;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * Задание 6.6: Jackson — JSON ↔ объекты.
 */
public class JsonTasks {
    /** Один ObjectMapper на всё приложение: он потокобезопасен и дорог в создании. */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Объект → JSON-строка. */
    public static String toJson(Object value) throws JsonProcessingException {
        return MAPPER.writeValueAsString(value);
    }

    /** JSON одного товара → ProductJson. */
    public static ProductJson parseProduct(String json) throws JsonProcessingException {
        return MAPPER.readValue(json, ProductJson.class);
    }

    /** JSON-массив товаров → List<ProductJson>. */
    public static List<ProductJson> parseProducts(String json) throws JsonProcessingException {
        return MAPPER.readValue(json, new TypeReference<List<ProductJson>>() { });
    }

    /**
     * Из ответа страницы каталога {"content":[{...},{...}],"totalElements":20}
     * достать название первого товара, не создавая классов (через JsonNode).
     */
    public static String firstProductName(String pageJson) throws JsonProcessingException {
        JsonNode root = MAPPER.readTree(pageJson);
        return root.get("content").get(0).get("name").asText();
    }
}
