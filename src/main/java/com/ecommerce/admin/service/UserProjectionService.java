package com.ecommerce.admin.service;

import com.ecommerce.admin.dto.AdminUserDetail;
import com.ecommerce.admin.dto.AdminUserSummary;
import com.ecommerce.admin.dto.UserRegisteredEvent;
import com.ecommerce.admin.entity.DashboardSnapshot;
import com.ecommerce.admin.entity.UserProjection;
import com.ecommerce.admin.exception.ApiException;
import com.ecommerce.admin.repository.DashboardSnapshotRepository;
import com.ecommerce.admin.repository.UserProjectionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class UserProjectionService {

    private final UserProjectionRepository userProjectionRepository;
    private final DashboardSnapshotRepository dashboardSnapshotRepository;
    private final OrderProjectionService orderProjectionService;

    public UserProjectionService(UserProjectionRepository userProjectionRepository,
                                 DashboardSnapshotRepository dashboardSnapshotRepository,
                                 OrderProjectionService orderProjectionService) {
        this.userProjectionRepository = userProjectionRepository;
        this.dashboardSnapshotRepository = dashboardSnapshotRepository;
        this.orderProjectionService = orderProjectionService;
    }

    @Transactional
    public void handleUserRegistered(UserRegisteredEvent event) {
        UserProjection projection = new UserProjection(
                event.userId(), event.email(), event.fullName(), event.occurredAt());
        userProjectionRepository.save(projection);
        recomputeUserKpi();
    }

    @Transactional
    public void recomputeUserKpi() {
        BigDecimal totalUsers = BigDecimal.valueOf(userProjectionRepository.countAll());
        DashboardSnapshot snapshot = dashboardSnapshotRepository.findByMetricName("total_users")
                .orElse(new DashboardSnapshot("total_users", totalUsers, Instant.now()));
        snapshot.setMetricValue(totalUsers);
        snapshot.setRecordedAt(Instant.now());
        dashboardSnapshotRepository.save(snapshot);
    }

    @Transactional(readOnly = true)
    public Page<AdminUserSummary> getUsers(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "registeredAt"));
        Page<UserProjection> userPage;
        if (search != null && !search.isBlank()) {
            userPage = userProjectionRepository.search(search, pageable);
        } else {
            userPage = userProjectionRepository.findAll(pageable);
        }
        return userPage.map(u -> new AdminUserSummary(
                u.getUserId(), u.getEmail(), u.getFullName(), u.getRegisteredAt()));
    }

    @Transactional(readOnly = true)
    public AdminUserDetail getUserDetail(Long userId) {
        UserProjection user = userProjectionRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        long orderCount = orderProjectionService.getOrderCountForUser(userId);
        return new AdminUserDetail(
                user.getUserId(), user.getEmail(), user.getFullName(),
                user.getRegisteredAt(), orderCount);
    }
}
