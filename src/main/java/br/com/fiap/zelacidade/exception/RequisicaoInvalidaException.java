package br.com.fiap.zelacidade.exception;

/** Lançada quando um dado enviado não faz sentido (ex.: status igual ao atual). O handler devolve 400. */
public class RequisicaoInvalidaException extends RuntimeException {

    public RequisicaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
