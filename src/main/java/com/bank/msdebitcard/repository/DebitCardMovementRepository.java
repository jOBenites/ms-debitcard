package com.bank.msdebitcard.repository;

import com.bank.msdebitcard.model.DebitCardMovement;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

/**
 * Repositorio reactivo para movimientos de tarjeta de debito.
 */
public interface DebitCardMovementRepository extends ReactiveMongoRepository<DebitCardMovement, String> {
}
