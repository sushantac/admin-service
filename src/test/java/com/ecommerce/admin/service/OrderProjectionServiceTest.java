package com.ecommerce.admin.service;

import com.ecommerce.admin.dto.AdminOrderDetail;
import com.ecommerce.admin.dto.AdminOrderSummary;
import com.ecommerce.admin.dto.OrderPlacedEvent;
import com.ecommerce.admin.dto.OrderStatusChangedEvent;
import com.ecommerce.admin.entity.DashboardSnapshot;
import com.ecommerce.admin.entity.OrderProjection;
import com.ecommerce.admin.exception.ApiException;
import com.ecommerce.admin.repository.DashboardSnapshotRepository;
import com.ecommerce.admin.repository.OrderProjectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderProjectionServiceTest {

    @Mock private OrderProjectionRepository orderProjectionRepository;
    @Mock private DashboardSnapshotRepository dashboardSnapshotRepository;

    @InjectMocks private OrderProjectionService orderProjectionService;

    private OrderProjection testOrder;

    @BeforeEach
    void setUp() {
        testOrder = new OrderProjection(
                100L, "ORD-001", 1L, "John Doe",
                "PLACED", new BigDecimal("99.99"), 3, Instant.now());
    }

    @Test
    void handleOrderPlaced_savesProjectionAndUpdatesDashboard() {
        OrderPlacedEvent event = new OrderPlacedEvent(
                "evt-1", 100L, "ORD-001", 1L, "John Doe",
                new BigDecimal("99.99"),
                List.of(new OrderPlacedEvent.OrderItem(1L, "Widget", 2, new BigDecimal("29.99")),
                        new OrderPlacedEvent.OrderItem(2L, "Gadget", 1, new BigDecimal("39.99"))),
                Instant.now());

        when(orderProjectionRepository.save(any(OrderProjection.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderProjectionRepository.countAll()).thenReturn(1L);
        when(orderProjectionRepository.sumTotalAmount()).thenReturn(new BigDecimal("99.99"));

        orderProjectionService.handleOrderPlaced(event);

        ArgumentCaptor<OrderProjection> captor = ArgumentCaptor.forClass(OrderProjection.class);
        verify(orderProjectionRepository).save(captor.capture());
        OrderProjection saved = captor.getValue();
        assertEquals(100L, saved.getOrderId());
        assertEquals("ORD-001", saved.getOrderNumber());
        assertEquals("PLACED", saved.getStatus());
        assertEquals(2, saved.getItemsCount());
    }

    @Test
    void handleOrderStatusChanged_updatesStatus() {
        when(orderProjectionRepository.findById(100L)).thenReturn(Optional.of(testOrder));
        when(orderProjectionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderProjectionRepository.countAll()).thenReturn(1L);
        when(orderProjectionRepository.sumTotalAmount()).thenReturn(new BigDecimal("99.99"));

        OrderStatusChangedEvent event = new OrderStatusChangedEvent(
                "evt-2", 100L, "ORD-001", "PLACED", "SHIPPED", Instant.now());

        orderProjectionService.handleOrderStatusChanged(event);

        assertEquals("SHIPPED", testOrder.getStatus());
        verify(orderProjectionRepository).save(testOrder);
    }

    @Test
    void handleOrderStatusChanged_orderNotFound_noException() {
        when(orderProjectionRepository.findById(999L)).thenReturn(Optional.empty());
        when(orderProjectionRepository.countAll()).thenReturn(0L);
        when(orderProjectionRepository.sumTotalAmount()).thenReturn(BigDecimal.ZERO);

        OrderStatusChangedEvent event = new OrderStatusChangedEvent(
                "evt-3", 999L, "ORD-999", "PLACED", "SHIPPED", Instant.now());

        assertDoesNotThrow(() -> orderProjectionService.handleOrderStatusChanged(event));
    }

    @Test
    void getOrders_withStatus_filtersCorrectly() {
        Page<OrderProjection> page = new PageImpl<>(List.of(testOrder));
        when(orderProjectionRepository.findByStatus(eq("PLACED"), any(Pageable.class))).thenReturn(page);

        var result = orderProjectionService.getOrders("PLACED", 0, 10);

        assertEquals(1, result.getContent().size());
        assertEquals("ORD-001", result.getContent().get(0).orderNumber());
    }

    @Test
    void getOrders_noStatus_returnsAll() {
        Page<OrderProjection> page = new PageImpl<>(List.of(testOrder));
        when(orderProjectionRepository.findAll(any(Pageable.class))).thenReturn(page);

        var result = orderProjectionService.getOrders(null, 0, 10);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getOrderDetail_found() {
        when(orderProjectionRepository.findById(100L)).thenReturn(Optional.of(testOrder));

        AdminOrderDetail detail = orderProjectionService.getOrderDetail(100L);

        assertEquals("ORD-001", detail.orderNumber());
        assertEquals("John Doe", detail.userName());
    }

    @Test
    void getOrderDetail_notFound_throws404() {
        when(orderProjectionRepository.findById(999L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
                () -> orderProjectionService.getOrderDetail(999L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void updateOrderStatus_found() {
        when(orderProjectionRepository.findById(100L)).thenReturn(Optional.of(testOrder));
        when(orderProjectionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderProjectionRepository.countAll()).thenReturn(1L);
        when(orderProjectionRepository.sumTotalAmount()).thenReturn(new BigDecimal("99.99"));

        orderProjectionService.updateOrderStatus(100L, "DELIVERED", "admin@test.com");

        assertEquals("DELIVERED", testOrder.getStatus());
        verify(orderProjectionRepository).save(testOrder);
    }

    @Test
    void updateOrderStatus_notFound_throws404() {
        when(orderProjectionRepository.findById(999L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
                () -> orderProjectionService.updateOrderStatus(999L, "DELIVERED", "admin@test.com"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }
}
