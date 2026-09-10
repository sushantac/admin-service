package com.ecommerce.admin.service;

import com.ecommerce.admin.dto.AdminUserDetail;
import com.ecommerce.admin.dto.AdminUserSummary;
import com.ecommerce.admin.dto.UserRegisteredEvent;
import com.ecommerce.admin.entity.DashboardSnapshot;
import com.ecommerce.admin.entity.UserProjection;
import com.ecommerce.admin.exception.ApiException;
import com.ecommerce.admin.repository.DashboardSnapshotRepository;
import com.ecommerce.admin.repository.UserProjectionRepository;
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

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProjectionServiceTest {

    @Mock private UserProjectionRepository userProjectionRepository;
    @Mock private DashboardSnapshotRepository dashboardSnapshotRepository;
    @Mock private OrderProjectionService orderProjectionService;

    @InjectMocks private UserProjectionService userProjectionService;

    private UserProjection testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserProjection(1L, "test@example.com", "Test User", Instant.now());
    }

    @Test
    void handleUserRegistered_savesProjection() {
        UserRegisteredEvent event = new UserRegisteredEvent(
                "evt-1", 1L, "test@example.com", "Test User", Instant.now());

        when(userProjectionRepository.save(any(UserProjection.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userProjectionRepository.countAll()).thenReturn(1L);

        userProjectionService.handleUserRegistered(event);

        ArgumentCaptor<UserProjection> captor = ArgumentCaptor.forClass(UserProjection.class);
        verify(userProjectionRepository).save(captor.capture());
        assertEquals(1L, captor.getValue().getUserId());
        assertEquals("test@example.com", captor.getValue().getEmail());
    }

    @Test
    void getUsers_withSearch() {
        Page<UserProjection> page = new PageImpl<>(List.of(testUser));
        when(userProjectionRepository.search(eq("test"), any(Pageable.class))).thenReturn(page);

        var result = userProjectionService.getUsers("test", 0, 10);

        assertEquals(1, result.getContent().size());
        assertEquals("test@example.com", result.getContent().get(0).email());
    }

    @Test
    void getUsers_noSearch_returnsAll() {
        Page<UserProjection> page = new PageImpl<>(List.of(testUser));
        when(userProjectionRepository.findAll(any(Pageable.class))).thenReturn(page);

        var result = userProjectionService.getUsers(null, 0, 10);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getUserDetail_found() {
        when(userProjectionRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(orderProjectionService.getOrderCountForUser(1L)).thenReturn(5L);

        AdminUserDetail detail = userProjectionService.getUserDetail(1L);

        assertEquals("test@example.com", detail.email());
        assertEquals(5L, detail.orderCount());
    }

    @Test
    void getUserDetail_notFound_throws404() {
        when(userProjectionRepository.findById(999L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
                () -> userProjectionService.getUserDetail(999L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }
}
