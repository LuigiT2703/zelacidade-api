package br.com.fiap.zelacidade.exception;

/**
 * Lançada quando o pedido é válido, mas viola uma regra de negócio do ZelaCidade
 * (ex.: apoiar duas vezes, excluir ocorrência resolvida). O handler devolve 409.
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
