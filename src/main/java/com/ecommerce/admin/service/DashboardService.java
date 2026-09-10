package com.ecommerce.admin.service;

import com.ecommerce.admin.dto.AdminOrderSummary;
import com.ecommerce.admin.dto.DashboardResponse;
import com.ecommerce.admin.entity.DashboardSnapshot;
import com.ecommerce.admin.repository.DashboardSnapshotRepository;
import com.ecommerce.admin.repository.OrderProjectionRepository;
import com.ecommerce.admin.repository.UserProjectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DashboardService {

    private final DashboardSnapshotRepository dashboardSnapshotRepository;
    private final OrderProjectionRepository orderProjectionRepository;
    private final UserProjectionRepository userProjectionRepository;
    private final OrderProjectionService orderProjectionService;

    public DashboardService(DashboardSnapshotRepository dashboardSnapshotRepository,
                            OrderProjectionRepository orderProjectionRepository,
                            UserProjectionRepository userProjectionRepository,
                            OrderProjectionService orderProjectionService) {
        this.dashboardSnapshotRepository = dashboardSnapshotRepository;
        this.orderProjectionRepository = orderProjectionRepository;
        this.userProjectionRepository = userProjectionRepository;
        this.orderProjectionService = orderProjectionService;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        BigDecimal totalOrders = getMetric("total_orders");
        BigDecimal totalRevenue = getMetric("total_revenue");
        BigDecimal totalUsers = getMetric("total_users");
        List<AdminOrderSummary> recentOrders = orderProjectionService.getRecentOrders(10);
        return new DashboardResponse(totalOrders, totalRevenue, totalUsers, recentOrders);
    }

    private BigDecimal getMetric(String name) {
        return dashboardSnapshotRepository.findByMetricName(name)
                .map(DashboardSnapshot::getMetricValue)
                .orElse(BigDecimal.ZERO);
    }
}
