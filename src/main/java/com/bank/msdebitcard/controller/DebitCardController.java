package com.bank.msdebitcard.controller;

import com.bank.msdebitcard.dto.DebitCardRequest;
import com.bank.msdebitcard.dto.DebitCardResponse;
import com.bank.msdebitcard.dto.PaymentRequest;
import com.bank.msdebitcard.model.DebitCard;
import com.bank.msdebitcard.service.DebitCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Controlador REST reactivo para tarjetas de debito.
 */
@RestController
@RequestMapping("/debit-cards")
@RequiredArgsConstructor
public class DebitCardController {

    private final DebitCardService debitCardService;

    /**
     * Crea una tarjeta de debito.
     *
     * @param request datos de la tarjeta
     * @return Mono con la tarjeta creada
     */
    @PostMapping
    public Mono<ResponseEntity<DebitCardResponse>> create(@RequestBody DebitCardRequest request) {
        return debitCardService.create(request.getCustomerId(), request.getPrimaryAccountId(), request.getCardNumber())
                .map(this::toResponse)
                .map(card -> ResponseEntity.status(HttpStatus.CREATED).body(card));
    }

    /**
     * Busca una tarjeta por ID.
     *
     * @param id identificador
     * @return Mono con respuesta o 404
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<DebitCardResponse>> getById(@PathVariable String id) {
        return debitCardService.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Lista todas las tarjetas.
     *
     * @return Flux con tarjetas
     */
    @GetMapping
    public Flux<DebitCardResponse> getAll() {
        return debitCardService.findAll().map(this::toResponse);
    }

    /**
     * Actualiza la cuenta principal asociada.
     *
     * @param id identificador
     * @param request nuevos datos
     * @return Mono con respuesta o 404
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<DebitCardResponse>> update(@PathVariable String id,
                                                        @RequestBody DebitCardRequest request) {
        return debitCardService.update(id, request.getPrimaryAccountId())
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Elimina una tarjeta.
     *
     * @param id identificador
     * @return Mono con 204 o 404
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable String id) {
        return debitCardService.delete(id)
                .flatMap(deleted -> {
                    if (deleted) {
                        return Mono.just(ResponseEntity.noContent().<Void>build());
                    }
                    return Mono.just(ResponseEntity.notFound().build());
                });
    }

    /**
     * Registra un pago con la tarjeta.
     *
     * @param id identificador de la tarjeta
     * @param request monto del pago
     * @return Mono con movimiento o 404
     */
    @PostMapping("/{id}/payments")
    public Mono<ResponseEntity<DebitCardResponse>> payment(@PathVariable String id,
                                                          @RequestBody PaymentRequest request) {
        return debitCardService.makePayment(id, request.getAmount())
                .flatMap(movement -> debitCardService.findById(id)
                        .map(this::toResponse)
                        .map(ResponseEntity.status(HttpStatus.CREATED)::body))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    private DebitCardResponse toResponse(DebitCard card) {
        DebitCardResponse response = new DebitCardResponse();
        response.setId(card.getId());
        response.setCustomerId(card.getCustomerId());
        response.setPrimaryAccountId(card.getPrimaryAccountId());
        response.setCardNumber(card.getCardNumber());
        response.setStatus(card.getStatus());
        return response;
    }
}
