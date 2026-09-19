package com.bank.msdebitcard.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO de respuesta para una tarjeta de debito.
 */
@Getter
@Setter
public class DebitCardResponse {

    private String id;
    private String customerId;
    private String primaryAccountId;
    private String cardNumber;
    private String status;
}
