package com.bank.msdebitcard.repository;

import com.bank.msdebitcard.model.WalletLinkView;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

/**
 * Repositorio reactivo para la vista de vinculo wallet-tarjeta de debito.
 * Se alimenta exclusivamente del evento bank.wallet.linked-to-debitcard.
 */
public interface WalletLinkViewRepository extends ReactiveMongoRepository<WalletLinkView, String> {

    /**
     * Busca el vinculo por celular del monedero.
     *
     * @param phoneNumber celular del monedero
     * @return Mono con el vinculo o vacio
     */
    Mono<WalletLinkView> findByPhoneNumber(String phoneNumber);
}
