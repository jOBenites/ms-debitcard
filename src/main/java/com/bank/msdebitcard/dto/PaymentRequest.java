package com.bank.msdebitcard.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Solicitud de pago con tarjeta de debito.
 */
@Getter
@Setter
public class PaymentRequest {

    private BigDecimal amount;
}
