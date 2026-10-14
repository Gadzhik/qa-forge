package ru.qacourse.shop.product;

public enum Category {
    BURGER("Бургеры"), SIDE("Закуски"), DRINK("Напитки"), DESSERT("Десерты");

    private final String title;

    Category(String title) { this.title = title; }

    public String title() { return title; }
}
