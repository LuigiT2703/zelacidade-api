package br.com.fiap.zelacidade.model;

import java.time.LocalDateTime;

/** Uma linha do histórico de uma ocorrência (lado N do relacionamento 1:N). */
public class Atualizacao {

    private Long id;
    private Long ocorrenciaId;
    private StatusOcorrencia status;
    private String comentario;
    private String responsavel;
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
