package br.com.fiap.zelacidade.exception;

/** Lançada quando o id pedido não existe no banco. O handler devolve 404. */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
