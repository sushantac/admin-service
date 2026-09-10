package com.ecommerce.admin.consumer;

import com.ecommerce.admin.dto.UserRegisteredEvent;
import com.ecommerce.admin.entity.IdempotencyKey;
import com.ecommerce.admin.repository.IdempotencyKeyRepository;
import com.ecommerce.admin.service.UserProjectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class UserRegisteredConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserRegisteredConsumer.class);

    private final UserProjectionService userProjectionService;
    private final IdempotencyKeyRepository idempotencyKeyRepository;

    public UserRegisteredConsumer(UserProjectionService userProjectionService,
                                  IdempotencyKeyRepository idempotencyKeyRepository) {
        this.userProjectionService = userProjectionService;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
    }

    @KafkaListener(topics = "auth.user.registered", groupId = "admin-service")
    @Transactional
    public void onUserRegistered(UserRegisteredEvent event, Acknowledgment ack) {
        if (event.eventId() == null || idempotencyKeyRepository.existsByEventId(event.eventId())) {
            log.debug("Duplicate or null eventId, skipping: {}", event.eventId());
            ack.acknowledge();
            return;
        }
        userProjectionService.handleUserRegistered(event);
        idempotencyKeyRepository.save(new IdempotencyKey(event.eventId(), "auth.user.registered", Instant.now()));
        ack.acknowledge();
    }
}
