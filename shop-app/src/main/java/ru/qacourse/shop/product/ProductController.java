package ru.qacourse.shop.product;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.qacourse.shop.auth.CurrentUser;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Товары")
public class ProductController {

    public record ProductRequest(@NotBlank @Size(max = 100) String name,
                                 @NotNull @Size(max = 500) String description,
                                 @NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal price,
                                 @NotNull Category category,
                                 @Min(0) int stock,
                                 String emoji) {
        Product toEntity() {
            return new Product(name, description, price, category, stock, emoji == null ? "🍔" : emoji);
        }
    }

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Список товаров с поиском, фильтром, сортировкой (sort=price,desc) и пагинацией")
    public PageResponse<Product> list(@RequestParam(required = false) String search,
                                      @RequestParam(required = false) Category category,
                                      @RequestParam(required = false) String sort,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        return service.search(search, category, sort, page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Товар по id")
    public Product get(@PathVariable long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать товар (только ADMIN)")
    public Product create(@Valid @RequestBody ProductRequest body, HttpServletRequest req) {
        CurrentUser.requireAdmin(req);
        return service.create(body.toEntity());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Изменить товар (только ADMIN)")
    public Product update(@PathVariable long id, @Valid @RequestBody ProductRequest body, HttpServletRequest req) {
        CurrentUser.requireAdmin(req);
        return service.update(id, body.toEntity());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить товар (только ADMIN)")
    public void delete(@PathVariable long id, HttpServletRequest req) {
        CurrentUser.requireAdmin(req);
        service.delete(id);
    }
}
