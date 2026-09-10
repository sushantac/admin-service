package com.ecommerce.admin.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record AdminOrderDetail(
        Long orderId,
        String orderNumber,
        Long userId,
        String userName,
        String status,
        BigDecimal totalAmount,
        Integer itemsCount,
        Instant placedAt,
        Instant updatedAt
) {}
