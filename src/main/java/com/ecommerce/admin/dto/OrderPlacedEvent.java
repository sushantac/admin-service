package com.ecommerce.admin.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderPlacedEvent(
        String eventId,
        Long orderId,
        String orderNumber,
        Long userId,
        String userName,
        BigDecimal totalAmount,
        List<OrderItem> items,
        Instant occurredAt
) {
    public record OrderItem(Long productId, String productName, Integer quantity, BigDecimal price) {}
}
