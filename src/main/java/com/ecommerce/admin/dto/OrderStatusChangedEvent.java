package com.ecommerce.admin.dto;

import java.time.Instant;

public record OrderStatusChangedEvent(
        String eventId,
        Long orderId,
        String orderNumber,
        String oldStatus,
        String newStatus,
        Instant changedAt
) {}
