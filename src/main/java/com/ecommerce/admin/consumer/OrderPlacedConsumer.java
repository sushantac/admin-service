package com.ecommerce.admin.consumer;

import com.ecommerce.admin.dto.OrderPlacedEvent;
import com.ecommerce.admin.entity.IdempotencyKey;
import com.ecommerce.admin.repository.IdempotencyKeyRepository;
import com.ecommerce.admin.service.OrderProjectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class OrderPlacedConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderPlacedConsumer.class);

    private final OrderProjectionService orderProjectionService;
    private final IdempotencyKeyRepository idempotencyKeyRepository;

    public OrderPlacedConsumer(OrderProjectionService orderProjectionService,
                               IdempotencyKeyRepository idempotencyKeyRepository) {
        this.orderProjectionService = orderProjectionService;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
    }

    @KafkaListener(topics = "order.placed", groupId = "admin-service")
    @Transactional
    public void onOrderPlaced(OrderPlacedEvent event, Acknowledgment ack) {
        if (event.eventId() == null || idempotencyKeyRepository.existsByEventId(event.eventId())) {
            log.debug("Duplicate or null eventId, skipping: {}", event.eventId());
            ack.acknowledge();
            return;
        }
        orderProjectionService.handleOrderPlaced(event);
        idempotencyKeyRepository.save(new IdempotencyKey(event.eventId(), "order.placed", Instant.now()));
        ack.acknowledge();
    }
}
