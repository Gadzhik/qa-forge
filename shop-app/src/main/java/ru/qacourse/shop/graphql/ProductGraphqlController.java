package ru.qacourse.shop.graphql;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import ru.qacourse.shop.bugs.Bug;
import ru.qacourse.shop.bugs.BugRegistry;
import ru.qacourse.shop.product.Category;
import ru.qacourse.shop.product.Product;
import ru.qacourse.shop.product.ProductRepository;
import ru.qacourse.shop.product.Review;
import ru.qacourse.shop.product.ReviewRepository;

import java.math.RoundingMode;
import java.util.List;

/** GraphQL-API меню: POST /graphql, интерактивная консоль — /graphiql. Схема: resources/graphql/schema.graphqls. */
@Controller
public class ProductGraphqlController {
    private final ProductRepository products;
    private final ReviewRepository reviews;
    private final BugRegistry bugs;

    public ProductGraphqlController(ProductRepository products, ReviewRepository reviews, BugRegistry bugs) {
        this.products = products;
        this.reviews = reviews;
        this.bugs = bugs;
    }

    @QueryMapping
    public List<Product> products(@Argument Category category) {
        return products.findAll().stream().filter(p -> category == null || p.getCategory() == category).toList();
    }

    @QueryMapping
    public Product product(@Argument long id) {
        return products.findById(id).orElse(null);
    }

    @SchemaMapping(typeName = "Product", field = "price")
    public double price(Product p) {
        return bugs.isOn(Bug.BUG_901) ? p.getPrice().setScale(0, RoundingMode.DOWN).doubleValue() : p.getPrice().doubleValue();
    }

    @SchemaMapping(typeName = "Product", field = "reviews")
    public List<Review> reviews(Product p) {
        return reviews.findByProductIdOrderByIdAsc(p.getId());
    }
}
