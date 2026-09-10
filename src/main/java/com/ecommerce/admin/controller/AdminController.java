package com.ecommerce.admin.controller;

import com.ecommerce.admin.dto.*;
import com.ecommerce.admin.service.AuditService;
import com.ecommerce.admin.service.DashboardService;
import com.ecommerce.admin.service.OrderProjectionService;
import com.ecommerce.admin.service.UserProjectionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final DashboardService dashboardService;
    private final OrderProjectionService orderProjectionService;
    private final UserProjectionService userProjectionService;
    private final AuditService auditService;

    public AdminController(DashboardService dashboardService,
                           OrderProjectionService orderProjectionService,
                           UserProjectionService userProjectionService,
                           AuditService auditService) {
        this.dashboardService = dashboardService;
        this.orderProjectionService = orderProjectionService;
        this.userProjectionService = userProjectionService;
        this.auditService = auditService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboard());
    }

    @GetMapping("/orders")
    public ResponseEntity<PaginatedResponse<AdminOrderSummary>> getOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AdminOrderSummary> orderPage = orderProjectionService.getOrders(status, page, size);
        return ResponseEntity.ok(new PaginatedResponse<>(
                orderPage.getContent(), orderPage.getNumber(), orderPage.getSize(),
                orderPage.getTotalElements(), orderPage.getTotalPages()));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<AdminOrderDetail> getOrderDetail(@PathVariable Long id) {
        return ResponseEntity.ok(orderProjectionService.getOrderDetail(id));
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<Void> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String performedBy = jwt.getSubject();
        orderProjectionService.updateOrderStatus(id, request.status(), performedBy);
        auditService.record("ORDER", id, "STATUS_CHANGED", performedBy,
                "{\"newStatus\":\"" + request.status() + "\"}");
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users")
    public ResponseEntity<PaginatedResponse<AdminUserSummary>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AdminUserSummary> userPage = userProjectionService.getUsers(search, page, size);
        return ResponseEntity.ok(new PaginatedResponse<>(
                userPage.getContent(), userPage.getNumber(), userPage.getSize(),
                userPage.getTotalElements(), userPage.getTotalPages()));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<AdminUserDetail> getUserDetail(@PathVariable Long id) {
        return ResponseEntity.ok(userProjectionService.getUserDetail(id));
    }
}
