package com.ecommerce.admin.repository;

import com.ecommerce.admin.entity.OrderProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface OrderProjectionRepository extends JpaRepository<OrderProjection, Long> {

    Page<OrderProjection> findByStatus(String status, Pageable pageable);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM OrderProjection o")
    BigDecimal sumTotalAmount();

    @Query("SELECT COUNT(o) FROM OrderProjection o")
    long countAll();

    @Query("SELECT COUNT(o) FROM OrderProjection o WHERE o.userId = :userId")
    long countByUserId(@Param("userId") Long userId);

    Optional<OrderProjection> findByOrderNumber(String orderNumber);
}
