package com.bank.msdebitcard.event;

import com.bank.msdebitcard.model.WalletLinkView;
import com.bank.msdebitcard.repository.DebitCardRepository;
import com.bank.msdebitcard.repository.WalletLinkViewRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Consumidor del evento bank.wallet.linked-to-debitcard.
 * Registra en la vista local el vinculo monedero-tarjeta y resuelve
 * la cuenta principal de la tarjeta para futuros abonos o cargas.
 */
@Component
@RequiredArgsConstructor
public class WalletLinkViewConsumer {

    private static final Logger log = LoggerFactory.getLogger(WalletLinkViewConsumer.class);

    private final WalletLinkViewRepository walletLinkViewRepository;
    private final DebitCardRepository debitCardRepository;

    /**
     * Consume bank.wallet.linked-to-debitcard y guarda o actualiza la vista
     * de vinculo, enriqueciendola con la cuenta principal de la tarjeta.
     *
     * @param payload datos del evento (walletId, phoneNumber, debitCardId, occurredAt)
     */
    @KafkaListener(topics = "bank.wallet.linked-to-debitcard", groupId = "ms-debitcard")
    public void onWalletLinkedToDebitCard(Map<String, Object> payload) {
        String walletId = (String) payload.get("walletId");
        String phoneNumber = (String) payload.get("phoneNumber");
        String debitCardId = (String) payload.get("debitCardId");
        walletLinkViewRepository.findById(walletId)
                .defaultIfEmpty(new WalletLinkView())
                .flatMap(view -> {
                    view.setWalletId(walletId);
                    view.setPhoneNumber(phoneNumber);
                    view.setDebitCardId(debitCardId);
                    view.setLinkedAt(LocalDateTime.now());
                    return debitCardRepository.findById(debitCardId)
                            .map(card -> {
                                view.setPrimaryAccountId(card.getPrimaryAccountId());
                                return view;
                            })
                            .defaultIfEmpty(view)
                            .flatMap(walletLinkViewRepository::save);
                })
                .subscribe(v -> log.info("Vinculo wallet {} a tarjeta {} registrado (cuenta principal {})",
                        walletId, debitCardId, v.getPrimaryAccountId()));
    }
}
