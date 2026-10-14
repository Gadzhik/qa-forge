package ru.qacourse.shop.product;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    @Enumerated(EnumType.STRING)
    private Category category;
    private int stock;
    private String emoji;

    protected Product() {
    }

    public Product(String name, String description, BigDecimal price, Category category, int stock, String emoji) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.stock = stock;
        this.emoji = emoji;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public Category getCategory() { return category; }
    public int getStock() { return stock; }
    public String getEmoji() { return emoji; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setCategory(Category category) { this.category = category; }
    public void setStock(int stock) { this.stock = stock; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
}
