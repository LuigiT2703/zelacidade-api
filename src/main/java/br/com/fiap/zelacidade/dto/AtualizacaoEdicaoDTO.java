package br.com.fiap.zelacidade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Corpo do PUT /atualizacoes/{id} (rota extra): corrige o texto de uma linha do histórico.
 * O status não muda aqui, porque o status da ocorrência só muda por uma nova atualização.
 */
public class AtualizacaoEdicaoDTO {

    @NotBlank(message = "Conte o que foi feito.")
    @Size(min = 10, max = 300, message = "O comentário deve ter de 10 a 300 caracteres.")
    private String comentario;

    @NotBlank(message = "Informe o responsável.")
    @Size(min = 3, max = 80, message = "O responsável deve ter de 3 a 80 caracteres.")
    private String responsavel;

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }
}
