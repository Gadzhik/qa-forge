package ru.qacourse.shop.product;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.qacourse.shop.auth.CurrentUser;
import ru.qacourse.shop.bugs.Bug;
import ru.qacourse.shop.bugs.BugRegistry;
import ru.qacourse.shop.common.ApiException;
import ru.qacourse.shop.user.User;

import java.util.List;

@RestController
@RequestMapping("/api/products/{productId}/reviews")
@Tag(name = "Отзывы")
public class ReviewController {

    public record ReviewRequest(@NotBlank @Size(max = 1000) String text, int rating) {
    }

    private final ReviewRepository reviews;
    private final ProductService products;
    private final BugRegistry bugs;

    public ReviewController(ReviewRepository reviews, ProductService products, BugRegistry bugs) {
        this.reviews = reviews;
        this.products = products;
        this.bugs = bugs;
    }

    @GetMapping
    @Operation(summary = "Отзывы о товаре")
    public List<Review> list(@PathVariable long productId) {
        products.get(productId);
        return reviews.findByProductIdOrderByIdAsc(productId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Оставить отзыв (нужен токен), rating от 1 до 5")
    public Review add(@PathVariable long productId, @Valid @RequestBody ReviewRequest body, HttpServletRequest req) {
        User user = CurrentUser.require(req);
        products.get(productId);
        int max = bugs.isOn(Bug.BUG_602) ? 10 : 5;
        if (body.rating() < 1 || body.rating() > max) {
            throw ApiException.badRequest("BAD_RATING", "Оценка должна быть от 1 до 5");
        }
        return reviews.save(new Review(productId, user.getUsername(), body.text(), body.rating()));
    }
}
