package br.com.fiap.zelacidade.model;

import java.time.LocalDate;

/** Associação Ocorrência x Cidadão: um morador confirmando um problema já registrado. */
public class Apoio {

    private Long id;
    private Long ocorrenciaId;
    private Long cidadaoId;
    private LocalDate dataApoio;

    public Apoio() {
    }

    public Apoio(Long ocorrenciaId, Long cidadaoId, LocalDate dataApoio) {
        this.ocorrenciaId = ocorrenciaId;
        this.cidadaoId = cidadaoId;
        this.dataApoio = dataApoio;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOcorrenciaId() { return ocorrenciaId; }
    public void setOcorrenciaId(Long ocorrenciaId) { this.ocorrenciaId = ocorrenciaId; }

    public Long getCidadaoId() { return cidadaoId; }
    public void setCidadaoId(Long cidadaoId) { this.cidadaoId = cidadaoId; }

    public LocalDate getDataApoio() { return dataApoio; }
    public void setDataApoio(LocalDate dataApoio) { this.dataApoio = dataApoio; }
}
