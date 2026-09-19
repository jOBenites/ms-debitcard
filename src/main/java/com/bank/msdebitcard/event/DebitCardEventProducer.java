package com.bank.msdebitcard.event;

import com.bank.msdebitcard.model.DebitCardMovement;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Productor de eventos del dominio de tarjeta de debito.
 */
@Component
@RequiredArgsConstructor
public class DebitCardEventProducer {

    private static final Logger log = LoggerFactory.getLogger(DebitCardEventProducer.class);
    private static final String MOVEMENT_RECORDED_TOPIC = "bank.movement.recorded";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publica el movimiento registrado para una tarjeta de debito.
     *
     * @param movement movimiento a publicar
     * @param productType tipo del producto afecado
     */
    public void publishMovementRecorded(DebitCardMovement movement, String productType) {
        Map<String, Object> payload = Map.of(
                "movementId", movement.getId(),
                "productId", movement.getDebitCardId(),
                "productType", productType,
                "movementType", movement.getMovementType(),
                "amount", movement.getAmount(),
                "occurredAt", LocalDateTime.now()
        );
        kafkaTemplate.send(MOVEMENT_RECORDED_TOPIC, movement.getId(), payload);
        log.info("Evento bank.movement.recorded publicado para tarjeta de debito {}", movement.getDebitCardId());
    }
}
