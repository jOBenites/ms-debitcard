package com.bank.msdebitcard.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Vista de lectura local del vinculo entre un monedero wallet y una tarjeta de debito.
 * Alimentada por el evento bank.wallet.linked-to-debitcard.
 * Permite saber a que cuenta principal acreditar o cargar sin consultar
 * la base de otros microservicios (database-per-service).
 */
@Getter
@Setter
@NoArgsConstructor
@Document(collection = "wallet_link_view")
public class WalletLinkView {

    @Id
    private String walletId;

    private String phoneNumber;

    private String debitCardId;

    private String primaryAccountId;

    private LocalDateTime linkedAt;

    /**
     * Constructor completo de la vista de vinculo wallet-tarjeta.
     *
     * @param walletId identificador del monedero
     * @param phoneNumber celular del monedero
     * @param debitCardId identificador de la tarjeta de debito vinculada
     * @param primaryAccountId cuenta principal asociada a la tarjeta
     * @param linkedAt fecha y hora del vinculo
     */
    public WalletLinkView(String walletId, String phoneNumber, String debitCardId,
                          String primaryAccountId, LocalDateTime linkedAt) {
        this.walletId = walletId;
        this.phoneNumber = phoneNumber;
        this.debitCardId = debitCardId;
        this.primaryAccountId = primaryAccountId;
        this.linkedAt = linkedAt;
    }
}
