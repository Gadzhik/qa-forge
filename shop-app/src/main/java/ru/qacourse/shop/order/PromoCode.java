package ru.qacourse.shop.order;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "promo_codes")
public class PromoCode {
    @Id
    private String code;
    private int percent;
    private LocalDate validUntil;

    protected PromoCode() {
    }

    public PromoCode(String code, int percent, LocalDate validUntil) {
        this.code = code;
        this.percent = percent;
        this.validUntil = validUntil;
    }

    public String getCode() { return code; }
    public int getPercent() { return percent; }
    public LocalDate getValidUntil() { return validUntil; }
}
