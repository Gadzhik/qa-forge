package ru.qacourse.shop.product;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long productId;
    private String author;
    private String text;
    private int rating;
    private Instant createdAt;

    protected Review() {
    }

    public Review(Long productId, String author, String text, int rating) {
        this.productId = productId;
        this.author = author;
        this.text = text;
        this.rating = rating;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public String getAuthor() { return author; }
    public String getText() { return text; }
    public int getRating() { return rating; }
    public Instant getCreatedAt() { return createdAt; }
}
