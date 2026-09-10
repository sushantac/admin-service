package com.ecommerce.admin.dto;

import java.time.Instant;

public record AdminUserDetail(
        Long userId,
        String email,
        String fullName,
        Instant registeredAt,
        Long orderCount
) {}
