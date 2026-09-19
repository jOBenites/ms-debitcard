package com.bank.msdebitcard.service;

import com.bank.msdebitcard.event.DebitCardEventProducer;
import com.bank.msdebitcard.model.DebitCard;
import com.bank.msdebitcard.repository.DebitCardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebitCardServiceTest {

    @Mock
    private DebitCardRepository debitCardRepository;

    @Mock
    private com.bank.msdebitcard.repository.DebitCardMovementRepository debitCardMovementRepository;

    @Mock
    private DebitCardEventProducer debitCardEventProducer;

    @InjectMocks
    private DebitCardService debitCardService;

    private DebitCard debitCard;

    @BeforeEach
    void setUp() {
        debitCard = new DebitCard("cust-1", "acc-1", "1234567890123456");
        debitCard.setId("dc-1");
    }

    @Test
    void createDebitCard_success() {
        when(debitCardRepository.save(any(DebitCard.class))).thenReturn(Mono.just(debitCard));

        StepVerifier.create(debitCardService.create("cust-1", "acc-1", "1234567890123456"))
                .assertNext(card -> {
                    org.junit.jupiter.api.Assertions.assertEquals("acc-1", card.getPrimaryAccountId());
                    org.junit.jupiter.api.Assertions.assertEquals("ACTIVE", card.getStatus());
                })
                .verifyComplete();
    }

    @Test
    void makePayment_validAmount_publishesEvent() {
        when(debitCardRepository.findById("dc-1")).thenReturn(Mono.just(debitCard));
        when(debitCardMovementRepository.save(any(com.bank.msdebitcard.model.DebitCardMovement.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(debitCardService.makePayment("dc-1", new BigDecimal("150.25")))
                .assertNext(movement -> {
                    org.junit.jupiter.api.Assertions.assertEquals("DEBIT_PAYMENT", movement.getMovementType());
                    org.junit.jupiter.api.Assertions.assertEquals(new BigDecimal("150.25"), movement.getAmount());
                })
                .verifyComplete();

        verify(debitCardEventProducer).publishMovementRecorded(any(), any());
    }

    @Test
    void makePayment_invalidAmount_throws() {
        StepVerifier.create(debitCardService.makePayment("dc-1", BigDecimal.ZERO))
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}
