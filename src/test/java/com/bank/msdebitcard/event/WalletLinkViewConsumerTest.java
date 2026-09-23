package com.bank.msdebitcard.event;

import com.bank.msdebitcard.model.DebitCard;
import com.bank.msdebitcard.model.WalletLinkView;
import com.bank.msdebitcard.repository.DebitCardRepository;
import com.bank.msdebitcard.repository.WalletLinkViewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para {@link WalletLinkViewConsumer}.
 * Valida el registro del vinculo wallet-tarjeta en la vista local.
 */
@ExtendWith(MockitoExtension.class)
class WalletLinkViewConsumerTest {

    @Mock
    private WalletLinkViewRepository walletLinkViewRepository;

    @Mock
    private DebitCardRepository debitCardRepository;

    @InjectMocks
    private WalletLinkViewConsumer walletLinkViewConsumer;

    @Test
    void onWalletLinkedToDebitCard_newWallet_savesViewWithPrimaryAccount() {
        when(walletLinkViewRepository.findById("wallet-1")).thenReturn(Mono.empty());
        DebitCard card = new DebitCard("cust-1", "acc-1", "1234");
        card.setId("dc-1");
        when(debitCardRepository.findById("dc-1")).thenReturn(Mono.just(card));
        when(walletLinkViewRepository.save(any(WalletLinkView.class)))
                .thenReturn(Mono.just(new WalletLinkView()));

        walletLinkViewConsumer.onWalletLinkedToDebitCard(Map.of(
                "walletId", "wallet-1",
                "phoneNumber", "+5491123456789",
                "debitCardId", "dc-1",
                "occurredAt", "2026-09-23T10:00:00"
        ));

        ArgumentCaptor<WalletLinkView> captor = ArgumentCaptor.forClass(WalletLinkView.class);
        verify(walletLinkViewRepository).save(captor.capture());
        assertEquals("wallet-1", captor.getValue().getWalletId());
        assertEquals("+5491123456789", captor.getValue().getPhoneNumber());
        assertEquals("dc-1", captor.getValue().getDebitCardId());
        assertEquals("acc-1", captor.getValue().getPrimaryAccountId());
    }

    @Test
    void onWalletLinkedToDebitCard_existingWallet_updatesView() {
        WalletLinkView existing = new WalletLinkView("wallet-1", "+5491111111111", "dc-old", "acc-1", null);
        when(walletLinkViewRepository.findById("wallet-1")).thenReturn(Mono.just(existing));
        DebitCard card = new DebitCard("cust-1", "acc-2", "1234");
        card.setId("dc-2");
        when(debitCardRepository.findById("dc-2")).thenReturn(Mono.just(card));
        when(walletLinkViewRepository.save(any(WalletLinkView.class))).thenReturn(Mono.just(existing));

        walletLinkViewConsumer.onWalletLinkedToDebitCard(Map.of(
                "walletId", "wallet-1",
                "phoneNumber", "+5491123456789",
                "debitCardId", "dc-2",
                "occurredAt", "2026-09-23T10:00:00"
        ));

        ArgumentCaptor<WalletLinkView> captor = ArgumentCaptor.forClass(WalletLinkView.class);
        verify(walletLinkViewRepository).save(captor.capture());
        assertEquals("dc-2", captor.getValue().getDebitCardId());
        assertEquals("acc-2", captor.getValue().getPrimaryAccountId());
    }

    @Test
    void onWalletLinkedToDebitCard_unknownCard_savesViewWithoutPrimaryAccount() {
        when(walletLinkViewRepository.findById("wallet-1")).thenReturn(Mono.empty());
        when(debitCardRepository.findById("dc-1")).thenReturn(Mono.empty());
        when(walletLinkViewRepository.save(any(WalletLinkView.class)))
                .thenReturn(Mono.just(new WalletLinkView()));

        walletLinkViewConsumer.onWalletLinkedToDebitCard(Map.of(
                "walletId", "wallet-1",
                "phoneNumber", "+5491123456789",
                "debitCardId", "dc-1",
                "occurredAt", "2026-09-23T10:00:00"
        ));

        ArgumentCaptor<WalletLinkView> captor = ArgumentCaptor.forClass(WalletLinkView.class);
        verify(walletLinkViewRepository).save(captor.capture());
        assertNull(captor.getValue().getPrimaryAccountId());
        verify(debitCardRepository, never()).save(any());
    }
}
