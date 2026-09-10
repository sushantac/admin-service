package com.ecommerce.admin.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record AdminOrderSummary(
        Long orderId,
        String orderNumber,
        String userName,
        String status,
        BigDecimal totalAmount,
        Integer itemsCount,
        Instant placedAt
) {}
