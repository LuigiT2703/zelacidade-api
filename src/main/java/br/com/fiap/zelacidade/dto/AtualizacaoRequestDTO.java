package br.com.fiap.zelacidade.dto;

import br.com.fiap.zelacidade.model.StatusOcorrencia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Corpo do POST /ocorrencias/{id}/atualizacoes ("NovaAtualizacao" no contrato). */
public class AtualizacaoRequestDTO {

    @NotNull(message = "Informe o novo status: ABERTA, EM_ANDAMENTO ou RESOLVIDA.")
    private StatusOcorrencia status;

    @NotBlank(message = "Conte o que foi feito.")
    @Size(min = 10, max = 300, message = "O comentário deve ter de 10 a 300 caracteres.")
    private String comentario;

    @NotBlank(message = "Informe o responsável.")
    @Size(min = 3, max = 80, message = "O responsável deve ter de 3 a 80 caracteres.")
    private String responsavel;

    public StatusOcorrencia getStatus() { return status; }
    public void setStatus(StatusOcorrencia status) { this.status = status; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }
}
