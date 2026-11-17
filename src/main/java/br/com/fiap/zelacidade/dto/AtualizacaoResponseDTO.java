package br.com.fiap.zelacidade.dto;

import br.com.fiap.zelacidade.model.StatusOcorrencia;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/** Objeto "Atualizacao" do contrato. */
public class AtualizacaoResponseDTO {

    private Long id;
    private Long ocorrenciaId;
    private StatusOcorrencia status;
    private String comentario;
    private String responsavel;

    // Formato fixo do contrato: aaaa-mm-ddThh:mm:ss (sempre com os segundos)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataHora;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOcorrenciaId() { return ocorrenciaId; }
    public void setOcorrenciaId(Long ocorrenciaId) { this.ocorrenciaId = ocorrenciaId; }

    public StatusOcorrencia getStatus() { return status; }
    public void setStatus(StatusOcorrencia status) { this.status = status; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
}
