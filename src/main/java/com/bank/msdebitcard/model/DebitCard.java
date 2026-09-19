package com.bank.msdebitcard.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Representa la tarjeta de debito asociada a una cuenta principal.
 */
@Getter
@Setter
@NoArgsConstructor
@Document(collection = "debit_card")
public class DebitCard {

    /** Estado activo. */
    public static final String STATUS_ACTIVE = "ACTIVE";

    @Id
    private String id;

    @Indexed
    private String customerId;

    @Indexed
    private String primaryAccountId;

    private String cardNumber;

    private String status;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * Constructor de tarjeta de debito.
     *
    * @param customerId identificador del cliente
    * @param primaryAccountId identificador de la cuenta principal
     * @param cardNumber numero de tarjeta
     */
    public DebitCard(String customerId, String primaryAccountId, String cardNumber) {
        this.customerId = customerId;
        this.primaryAccountId = primaryAccountId;
        this.cardNumber = cardNumber;
        this.status = STATUS_ACTIVE;
    }
}
