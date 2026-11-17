package br.com.fiap.zelacidade.exception;

/**
 * Embrulha a SQLException dos DAOs. A mensagem técnica fica só no log do servidor;
 * o cliente recebe um 500 com mensagem genérica (nunca SQL nem stack trace).
 */
public class AcessoDadosException extends RuntimeException {

    public AcessoDadosException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
