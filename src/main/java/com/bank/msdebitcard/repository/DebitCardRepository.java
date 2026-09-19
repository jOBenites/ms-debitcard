package com.bank.msdebitcard.repository;

import com.bank.msdebitcard.model.DebitCard;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

/**
 * Repositorio reactivo para tarjetas de debito.
 */
public interface DebitCardRepository extends ReactiveMongoRepository<DebitCard, String> {
}
