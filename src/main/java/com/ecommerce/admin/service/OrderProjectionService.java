package com.ecommerce.admin.service;

import com.ecommerce.admin.dto.AdminOrderSummary;
import com.ecommerce.admin.dto.OrderPlacedEvent;
import com.ecommerce.admin.dto.OrderStatusChangedEvent;
import com.ecommerce.admin.entity.DashboardSnapshot;
import com.ecommerce.admin.entity.OrderProjection;
import com.ecommerce.admin.repository.DashboardSnapshotRepository;
import com.ecommerce.admin.repository.OrderProjectionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class OrderProjectionService {

    private final OrderProjectionRepository orderProjectionRepository;
    private final DashboardSnapshotRepository dashboardSnapshotRepository;

    public OrderProjectionService(OrderProjectionRepository orderProjectionRepository,
                                  DashboardSnapshotRepository dashboardSnapshotRepository) {
        this.orderProjectionRepository = orderProjectionRepository;
        this.dashboardSnapshotRepository = dashboardSnapshotRepository;
    }

    @Transactional
    public void handleOrderPlaced(OrderPlacedEvent event) {
        int itemsCount = event.items() != null ? event.items().size() : 0;
        OrderProjection projection = new OrderProjection(
                event.orderId(),
                event.orderNumber(),
                event.userId(),
                event.userName(),
                "PLACED",
                event.totalAmount(),
                itemsCount,
                event.occurredAt()
        );
        orderProjectionRepository.save(projection);
        recomputeDashboardKpis();
    }

    @Transactional
    public void handleOrderStatusChanged(OrderStatusChangedEvent event) {
        orderProjectionRepository.findById(event.orderId()).ifPresent(op -> {
            op.setStatus(event.newStatus());
            op.setUpdatedAt(event.changedAt());
            orderProjectionRepository.save(op);
        });
        recomputeDashboardKpis();
    }

    @Transactional(readOnly = true)
    public Page<AdminOrderSummary> getOrders(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "placedAt"));
        Page<OrderProjection> orderPage;
        if (status != null && !status.isBlank()) {
            orderPage = orderProjectionRepository.findByStatus(status, pageable);
        } else {
            orderPage = orderProjectionRepository.findAll(pageable);
        }
        return orderPage.map(op -> new AdminOrderSummary(
                op.getOrderId(), op.getOrderNumber(), op.getUserName(),
                op.getStatus(), op.getTotalAmount(), op.getItemsCount(), op.getPlacedAt()));
    }

    @Transactional(readOnly = true)
    public com.ecommerce.admin.dto.AdminOrderDetail getOrderDetail(Long orderId) {
        OrderProjection op = orderProjectionRepository.findById(orderId)
                .orElseThrow(() -> new com.ecommerce.admin.exception.ApiException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Order not found"));
        return new com.ecommerce.admin.dto.AdminOrderDetail(
                op.getOrderId(), op.getOrderNumber(), op.getUserId(), op.getUserName(),
                op.getStatus(), op.getTotalAmount(), op.getItemsCount(),
                op.getPlacedAt(), op.getUpdatedAt());
    }

    @Transactional
    public void updateOrderStatus(Long orderId, String newStatus, String performedBy) {
        OrderProjection op = orderProjectionRepository.findById(orderId)
                .orElseThrow(() -> new com.ecommerce.admin.exception.ApiException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Order not found"));
        String oldStatus = op.getStatus();
        op.setStatus(newStatus);
        op.setUpdatedAt(Instant.now());
        orderProjectionRepository.save(op);
        recomputeDashboardKpis();
    }

    @Transactional
    public void recomputeDashboardKpis() {
        BigDecimal totalOrders = BigDecimal.valueOf(orderProjectionRepository.countAll());
        BigDecimal totalRevenue = orderProjectionRepository.sumTotalAmount();

        upsertMetric("total_orders", totalOrders);
        upsertMetric("total_revenue", totalRevenue);
    }

    private void upsertMetric(String name, BigDecimal value) {
        DashboardSnapshot snapshot = dashboardSnapshotRepository.findByMetricName(name)
                .orElse(new DashboardSnapshot(name, value, Instant.now()));
        snapshot.setMetricValue(value);
        snapshot.setRecordedAt(Instant.now());
        dashboardSnapshotRepository.save(snapshot);
    }

    public List<AdminOrderSummary> getRecentOrders(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "placedAt"));
        Page<OrderProjection> recent = orderProjectionRepository.findAll(pageable);
        return recent.map(op -> new AdminOrderSummary(
                op.getOrderId(), op.getOrderNumber(), op.getUserName(),
                op.getStatus(), op.getTotalAmount(), op.getItemsCount(), op.getPlacedAt())).getContent();
    }

    public long getOrderCountForUser(Long userId) {
        return orderProjectionRepository.countByUserId(userId);
    }
}
