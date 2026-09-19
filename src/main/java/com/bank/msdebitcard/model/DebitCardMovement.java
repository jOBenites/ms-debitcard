package com.bank.msdebitcard.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Movimiento generado por un pago con tarjeta de debito.
 */
@Getter
@Setter
@NoArgsConstructor
@Document(collection = "debit_card_movement")
public class DebitCardMovement {

    /** Tipo de movimiento. */
    public static final String TYPE_DEBIT_PAYMENT = "DEBIT_PAYMENT";

    @Id
    private String id;

    private String debitCardId;

    private String movementType;

    private BigDecimal amount;

    @CreatedDate
    private LocalDateTime occurredAt;

    /**
     * Constructor para un movimiento de debito.
     *
     * @param debitCardId identificador de la tarjeta
     * @param amount monto del pago
     */
    public DebitCardMovement(String debitCardId, BigDecimal amount) {
        this.debitCardId = debitCardId;
        this.movementType = TYPE_DEBIT_PAYMENT;
        this.amount = amount;
    }
}
