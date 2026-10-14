package ru.qacourse.m08;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Задание 8.7: HTTP-запросы к QA Burger встроенным в Java клиентом (без REST Assured).
 * baseUrl — адрес приложения, например http://localhost:8081.
 */
public class ShopHttp {
    private final String baseUrl;
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public ShopHttp(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /** Пример, готовый: GET /api/products/{id} → код ответа. */
    public int productStatus(long id) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/api/products/" + id))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode();
    }

    /** GET /api/products/{id} → поле "name" из JSON-ответа. */
    public String productName(long id) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/api/products/" + id)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return mapper.readTree(response.body()).get("name").asText();
    }

    /** GET /api/products?category=...&size=100 → значение "totalElements". */
    public int countInCategory(String category) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/api/products?size=100&category=" + category))
                .GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return mapper.readTree(response.body()).get("totalElements").asInt();
    }

    /**
     * POST /api/auth/login с JSON {"username": ..., "password": ...} и заголовком Content-Type: application/json.
     * Вернуть токен из ответа, а при коде, отличном от 200, — бросить IllegalStateException с кодом ответа в сообщении.
     */
    public String login(String username, String password) throws IOException, InterruptedException {
        String body = mapper.writeValueAsString(java.util.Map.of("username", username, "password", password));
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Вход не удался: HTTP " + response.statusCode());
        }
        return mapper.readTree(response.body()).get("token").asText();
    }

    /** GET /api/cart с заголовком Authorization: Bearer <token> → "totalQuantity". */
    public int cartQuantity(String token) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/api/cart"))
                .header("Authorization", "Bearer " + token)
                .GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode json = mapper.readTree(response.body());
        return json.get("totalQuantity").asInt();
    }
}
