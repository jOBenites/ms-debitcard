package com.bank.msdebitcard.service;

import com.bank.msdebitcard.event.DebitCardEventProducer;
import com.bank.msdebitcard.model.DebitCard;
import com.bank.msdebitcard.model.DebitCardMovement;
import com.bank.msdebitcard.repository.DebitCardMovementRepository;
import com.bank.msdebitcard.repository.DebitCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * Servicio reactivo para tarjetas de debito.
 */
@Service
@RequiredArgsConstructor
public class DebitCardService {

    private final DebitCardRepository debitCardRepository;
    private final DebitCardMovementRepository debitCardMovementRepository;
    private final DebitCardEventProducer debitCardEventProducer;

    /**
     * Crea una tarjeta de debito asociada a una cuenta principal.
     *
     * @param customerId identificador del cliente
     * @param primaryAccountId identificador de la cuenta principal
     * @param cardNumber numero de la tarjeta
     * @return Mono con la tarjeta creada
     */
    public Mono<DebitCard> create(String customerId, String primaryAccountId, String cardNumber) {
        if (customerId == null || customerId.isBlank()) {
            return Mono.error(new IllegalArgumentException("El cliente es obligatorio"));
        }
        if (primaryAccountId == null || primaryAccountId.isBlank()) {
            return Mono.error(new IllegalArgumentException("La cuenta principal es obligatoria"));
        }
        DebitCard debitCard = new DebitCard(customerId, primaryAccountId, cardNumber);
        return debitCardRepository.save(debitCard);
    }

    /**
     * Busca una tarjeta por ID.
     *
     * @param id identificador de la tarjeta
     * @return Mono con la tarjeta o vacio
     */
    public Mono<DebitCard> findById(String id) {
        return debitCardRepository.findById(id);
    }

    /**
     * Lista todas las tarjetas.
     *
     * @return Flux con las tarjetas
     */
    public Flux<DebitCard> findAll() {
        return debitCardRepository.findAll();
    }

    /**
     * Actualiza una tarjeta.
     *
     * @param id identificador de la tarjeta
     * @param primaryAccountId nueva cuenta principal
     * @return Mono con tarjeta actualizada o vacio
     */
    public Mono<DebitCard> update(String id, String primaryAccountId) {
        return debitCardRepository.findById(id)
                .flatMap(card -> {
                    if (primaryAccountId != null && !primaryAccountId.isBlank()) {
                        card.setPrimaryAccountId(primaryAccountId);
                    }
                    return debitCardRepository.save(card);
                });
    }

    /**
     * Elimina una tarjeta.
     *
     * @param id identificador de la tarjeta
     * @return Mono true si se elimino
     */
    public Mono<Boolean> delete(String id) {
        return debitCardRepository.existsById(id)
                .flatMap(exists -> {
                    if (exists) {
                        return debitCardRepository.deleteById(id).then(Mono.just(true));
                    }
                    return Mono.just(false);
                });
    }

    /**
     * Registra un pago con una tarjeta de debito.
     *
     * @param debitCardId identificador de la tarjeta
     * @param amount monto del pago
     * @return Mono con el movimiento registrado
     */
    public Mono<DebitCardMovement> makePayment(String debitCardId, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            return Mono.error(new IllegalArgumentException("El monto debe ser mayor a cero"));
        }
        return debitCardRepository.findById(debitCardId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Tarjeta de debito no encontrada")))
                .flatMap(card -> {
                    DebitCardMovement movement = new DebitCardMovement(card.getId(), amount);
                    return debitCardMovementRepository.save(movement)
                            .doOnNext(saved -> debitCardEventProducer.publishMovementRecorded(saved, "DEBIT_CARD"));
                });
    }
}
