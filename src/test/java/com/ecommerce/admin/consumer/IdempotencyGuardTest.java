package com.ecommerce.admin.consumer;

import com.ecommerce.admin.dto.OrderPlacedEvent;
import com.ecommerce.admin.dto.OrderStatusChangedEvent;
import com.ecommerce.admin.dto.UserRegisteredEvent;
import com.ecommerce.admin.entity.IdempotencyKey;
import com.ecommerce.admin.repository.IdempotencyKeyRepository;
import com.ecommerce.admin.service.OrderProjectionService;
import com.ecommerce.admin.service.UserProjectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdempotencyGuardTest {

    @Mock private IdempotencyKeyRepository idempotencyKeyRepository;
    @Mock private OrderProjectionService orderProjectionService;
    @Mock private UserProjectionService userProjectionService;
    @Mock private Acknowledgment ack;

    @InjectMocks private OrderPlacedConsumer orderPlacedConsumer;
    @InjectMocks private OrderStatusChangedConsumer orderStatusChangedConsumer;
    @InjectMocks private UserRegisteredConsumer userRegisteredConsumer;

    @Test
    void orderPlaced_duplicateEventId_isSkipped() {
        OrderPlacedEvent event = new OrderPlacedEvent(
                "dup-1", 100L, "ORD-001", 1L, "John",
                new BigDecimal("50"), List.of(), Instant.now());

        when(idempotencyKeyRepository.existsByEventId("dup-1")).thenReturn(true);

        orderPlacedConsumer.onOrderPlaced(event, ack);

        verifyNoInteractions(orderProjectionService);
        verify(idempotencyKeyRepository, never()).save(any());
        verify(ack).acknowledge();
    }

    @Test
    void orderPlaced_newEventId_isProcessed() {
        OrderPlacedEvent event = new OrderPlacedEvent(
                "new-1", 100L, "ORD-001", 1L, "John",
                new BigDecimal("50"), List.of(), Instant.now());

        when(idempotencyKeyRepository.existsByEventId("new-1")).thenReturn(false);

        orderPlacedConsumer.onOrderPlaced(event, ack);

        verify(orderProjectionService).handleOrderPlaced(event);
        verify(idempotencyKeyRepository).save(any(IdempotencyKey.class));
        verify(ack).acknowledge();
    }

    @Test
    void orderPlaced_nullEventId_isSkipped() {
        OrderPlacedEvent event = new OrderPlacedEvent(
                null, 100L, "ORD-001", 1L, "John",
                new BigDecimal("50"), List.of(), Instant.now());

        orderPlacedConsumer.onOrderPlaced(event, ack);

        verifyNoInteractions(orderProjectionService);
        verify(ack).acknowledge();
    }

    @Test
    void orderStatusChanged_duplicateEventId_isSkipped() {
        OrderStatusChangedEvent event = new OrderStatusChangedEvent(
                "dup-2", 100L, "ORD-001", "PLACED", "SHIPPED", Instant.now());

        when(idempotencyKeyRepository.existsByEventId("dup-2")).thenReturn(true);

        orderStatusChangedConsumer.onOrderStatusChanged(event, ack);

        verifyNoInteractions(orderProjectionService);
        verify(ack).acknowledge();
    }

    @Test
    void userRegistered_duplicateEventId_isSkipped() {
        UserRegisteredEvent event = new UserRegisteredEvent(
                "dup-3", 1L, "test@test.com", "Test", Instant.now());

        when(idempotencyKeyRepository.existsByEventId("dup-3")).thenReturn(true);

        userRegisteredConsumer.onUserRegistered(event, ack);

        verifyNoInteractions(userProjectionService);
        verify(ack).acknowledge();
    }

    @Test
    void userRegistered_newEventId_isProcessed() {
        UserRegisteredEvent event = new UserRegisteredEvent(
                "new-3", 1L, "test@test.com", "Test", Instant.now());

        when(idempotencyKeyRepository.existsByEventId("new-3")).thenReturn(false);

        userRegisteredConsumer.onUserRegistered(event, ack);

        verify(userProjectionService).handleUserRegistered(event);
        verify(idempotencyKeyRepository).save(any(IdempotencyKey.class));
        verify(ack).acknowledge();
    }
}
