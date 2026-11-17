package br.com.fiap.zelacidade.dto;

/** Resposta do POST /ocorrencias/{id}/apoios: { "ocorrenciaId": 1, "totalApoios": 4 }. */
public class ApoioResultadoDTO {

    private Long ocorrenciaId;
    private Integer totalApoios;

    public ApoioResultadoDTO() {
    }

    public ApoioResultadoDTO(Long ocorrenciaId, Integer totalApoios) {
        this.ocorrenciaId = ocorrenciaId;
        this.totalApoios = totalApoios;
    }

    public Long getOcorrenciaId() { return ocorrenciaId; }
    public void setOcorrenciaId(Long ocorrenciaId) { this.ocorrenciaId = ocorrenciaId; }

    public Integer getTotalApoios() { return totalApoios; }
    public void setTotalApoios(Integer totalApoios) { this.totalApoios = totalApoios; }
}
