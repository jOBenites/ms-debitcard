package com.bank.msdebitcard.event;

import com.bank.msdebitcard.model.DebitCardMovement;
import com.bank.msdebitcard.model.WalletLinkView;
import com.bank.msdebitcard.repository.DebitCardMovementRepository;
import com.bank.msdebitcard.repository.WalletLinkViewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para {@link WalletPaymentConsumer}.
 * Valida el debito del pago wallet contra la tarjeta enlazada al origen.
 */
@ExtendWith(MockitoExtension.class)
class WalletPaymentConsumerTest {

    @Mock
    private WalletLinkViewRepository walletLinkViewRepository;

    @Mock
    private DebitCardMovementRepository debitCardMovementRepository;

    @Mock
    private DebitCardEventProducer debitCardEventProducer;

    @InjectMocks
    private WalletPaymentConsumer walletPaymentConsumer;

    @Test
    void onPaymentSent_linkedSource_savesMovementAndPublishesEvent() {
        WalletLinkView link = new WalletLinkView("wallet-1", "+5491123456789", "dc-1", "acc-1", null);
        when(walletLinkViewRepository.findByPhoneNumber("+5491123456789")).thenReturn(Mono.just(link));
        when(debitCardMovementRepository.save(any(DebitCardMovement.class)))
                .thenAnswer(inv -> {
                    DebitCardMovement m = inv.getArgument(0);
                    m.setId("mv-1");
                    return Mono.just(m);
                });

        walletPaymentConsumer.onPaymentSent(Map.of(
                "paymentId", "pay-1",
                "sourcePhoneNumber", "+5491123456789",
                "targetPhoneNumber", "+5491123456790",
                "amount", new BigDecimal("25.00"),
                "occurredAt", "2026-09-23T10:00:00"
        ));

        ArgumentCaptor<DebitCardMovement> captor = ArgumentCaptor.forClass(DebitCardMovement.class);
        verify(debitCardMovementRepository).save(captor.capture());
        assertEquals("dc-1", captor.getValue().getDebitCardId());
        assertEquals(0, new BigDecimal("25.00").compareTo(captor.getValue().getAmount()));
        verify(debitCardEventProducer).publishMovementRecorded(any(DebitCardMovement.class), eq("DEBIT_CARD"));
    }

    @Test
    void onPaymentSent_unlinkedSource_doesNothing() {
        when(walletLinkViewRepository.findByPhoneNumber("+5491111111111")).thenReturn(Mono.empty());

        walletPaymentConsumer.onPaymentSent(Map.of(
                "paymentId", "pay-2",
                "sourcePhoneNumber", "+5491111111111",
                "targetPhoneNumber", "+5491123456790",
                "amount", new BigDecimal("10.00"),
                "occurredAt", "2026-09-23T10:00:00"
        ));

        verify(debitCardMovementRepository, never()).save(any());
        verify(debitCardEventProducer, never()).publishMovementRecorded(any(), any());
    }
}
