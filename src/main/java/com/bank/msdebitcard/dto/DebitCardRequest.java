package com.bank.msdebitcard.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO para crear o actualizar una tarjeta de debito.
 */
@Getter
@Setter
public class DebitCardRequest {

    private String customerId;
    private String primaryAccountId;
    private String cardNumber;
}
