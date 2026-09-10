package com.ecommerce.admin.service;

import com.ecommerce.admin.dto.AdminOrderSummary;
import com.ecommerce.admin.dto.DashboardResponse;
import com.ecommerce.admin.entity.DashboardSnapshot;
import com.ecommerce.admin.repository.DashboardSnapshotRepository;
import com.ecommerce.admin.repository.OrderProjectionRepository;
import com.ecommerce.admin.repository.UserProjectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private DashboardSnapshotRepository dashboardSnapshotRepository;
    @Mock private OrderProjectionRepository orderProjectionRepository;
    @Mock private UserProjectionRepository userProjectionRepository;
    @Mock private OrderProjectionService orderProjectionService;

    @InjectMocks private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        lenient().when(dashboardSnapshotRepository.findByMetricName("total_orders"))
                .thenReturn(Optional.of(new DashboardSnapshot("total_orders", new BigDecimal("42"), Instant.now())));
        lenient().when(dashboardSnapshotRepository.findByMetricName("total_revenue"))
                .thenReturn(Optional.of(new DashboardSnapshot("total_revenue", new BigDecimal("12500.50"), Instant.now())));
        lenient().when(dashboardSnapshotRepository.findByMetricName("total_users"))
                .thenReturn(Optional.of(new DashboardSnapshot("total_users", new BigDecimal("15"), Instant.now())));
        lenient().when(orderProjectionService.getRecentOrders(10)).thenReturn(Collections.emptyList());
    }

    @Test
    void getDashboard_returnsCorrectKpis() {
        DashboardResponse response = dashboardService.getDashboard();

        assertEquals(new BigDecimal("42"), response.totalOrders());
        assertEquals(new BigDecimal("12500.50"), response.totalRevenue());
        assertEquals(new BigDecimal("15"), response.totalUsers());
        assertNotNull(response.recentOrders());
    }

    @Test
    void getDashboard_returnsZeroesWhenNoSnapshots() {
        when(dashboardSnapshotRepository.findByMetricName(anyString())).thenReturn(Optional.empty());

        DashboardResponse response = dashboardService.getDashboard();

        assertEquals(BigDecimal.ZERO, response.totalOrders());
        assertEquals(BigDecimal.ZERO, response.totalRevenue());
        assertEquals(BigDecimal.ZERO, response.totalUsers());
    }
}
