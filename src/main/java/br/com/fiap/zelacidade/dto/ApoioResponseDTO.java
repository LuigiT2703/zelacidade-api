package br.com.fiap.zelacidade.dto;

import java.time.LocalDate;

/** Um apoio nas rotas extras (/apoios). Só ids, sem o nome da pessoa (LGPD). */
public class ApoioResponseDTO {

    private Long id;
    private Long ocorrenciaId;
    private Long cidadaoId;
    private LocalDate dataApoio;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOcorrenciaId() { return ocorrenciaId; }
    public void setOcorrenciaId(Long ocorrenciaId) { this.ocorrenciaId = ocorrenciaId; }

    public Long getCidadaoId() { return cidadaoId; }
    public void setCidadaoId(Long cidadaoId) { this.cidadaoId = cidadaoId; }

    public LocalDate getDataApoio() { return dataApoio; }
    public void setDataApoio(LocalDate dataApoio) { this.dataApoio = dataApoio; }
}
