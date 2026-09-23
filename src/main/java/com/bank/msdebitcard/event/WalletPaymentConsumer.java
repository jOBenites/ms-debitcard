package com.bank.msdebitcard.event;

import com.bank.msdebitcard.model.DebitCardMovement;
import com.bank.msdebitcard.repository.DebitCardMovementRepository;
import com.bank.msdebitcard.repository.WalletLinkViewRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Consumidor del evento bank.wallet.payment-sent.
 * Si el celular origen tiene tarjeta vinculada, registra el debito
 * contra esa tarjeta y publica bank.movement.recorded para reportes.
 */
@Component
@RequiredArgsConstructor
public class WalletPaymentConsumer {

    private static final Logger log = LoggerFactory.getLogger(WalletPaymentConsumer.class);
    private static final String PRODUCT_TYPE = "DEBIT_CARD";

    private final WalletLinkViewRepository walletLinkViewRepository;
    private final DebitCardMovementRepository debitCardMovementRepository;
    private final DebitCardEventProducer debitCardEventProducer;

    /**
     * Consume bank.wallet.payment-sent y registra el movimiento de debito
     * en la tarjeta enlazada al celular origen (si existe vinculo).
     *
     * @param payload datos del evento (paymentId, sourcePhoneNumber, targetPhoneNumber, amount, occurredAt)
     */
    @KafkaListener(topics = "bank.wallet.payment-sent", groupId = "ms-debitcard")
    public void onPaymentSent(Map<String, Object> payload) {
        String sourcePhoneNumber = (String) payload.get("sourcePhoneNumber");
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());
        walletLinkViewRepository.findByPhoneNumber(sourcePhoneNumber)
                .flatMap(link -> {
                    DebitCardMovement movement = new DebitCardMovement(link.getDebitCardId(), amount);
                    return debitCardMovementRepository.save(movement)
                            .doOnNext(saved -> debitCardEventProducer.publishMovementRecorded(saved, PRODUCT_TYPE));
                })
                .switchIfEmpty(Mono.fromRunnable(
                        () -> log.info("Sin vinculo para celular origen {}, pago wallet no debitado", sourcePhoneNumber)))
                .subscribe(
                        movement -> log.info("Pago wallet {} debitado a tarjeta {} por {}",
                                payload.get("paymentId"), movement.getDebitCardId(), amount),
                        error -> log.error("Error al registrar pago wallet para {}", sourcePhoneNumber, error));
    }
}
