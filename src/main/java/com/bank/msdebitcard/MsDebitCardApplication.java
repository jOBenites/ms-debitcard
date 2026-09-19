package com.bank.msdebitcard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Microservicio de tarjetas de debito del sistema bancario.
 * Gestiona la asociacion de tarjetas a cuentas principales y registra pagos
 * directos contra el saldo disponible de la cuenta principal.
 */
@EnableMongoAuditing
@SpringBootApplication
public class MsDebitCardApplication {

    /**
     * Inicio de la aplicacion.
     *
     * @param args argumentos de inicio
     */
    public static void main(String[] args) {
        SpringApplication.run(MsDebitCardApplication.class, args);
    }
}
