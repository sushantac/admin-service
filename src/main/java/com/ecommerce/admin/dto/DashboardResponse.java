package com.ecommerce.admin.dto;

import java.math.BigDecimal;

public record DashboardResponse(
        BigDecimal totalOrders,
        BigDecimal totalRevenue,
        BigDecimal totalUsers,
        java.util.List<AdminOrderSummary> recentOrders
) {}
