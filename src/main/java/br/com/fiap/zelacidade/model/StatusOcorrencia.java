package br.com.fiap.zelacidade.model;

/** Andamento de uma ocorrência. Só ABERTA e EM_ANDAMENTO contam como "pendentes" na fila. */
public enum StatusOcorrencia {
    ABERTA,
    EM_ANDAMENTO,
    RESOLVIDA;

    public boolean pendente() {
        return this != RESOLVIDA;
    }
}
