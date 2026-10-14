package ru.qacourse.shop.order;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PromoRepository extends JpaRepository<PromoCode, String> {
}
