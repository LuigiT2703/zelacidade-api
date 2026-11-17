package br.com.fiap.zelacidade.dto;

import br.com.fiap.zelacidade.model.Categoria;
import br.com.fiap.zelacidade.model.StatusOcorrencia;

import java.time.LocalDate;

/**
 * Objeto "Ocorrencia" do contrato. Por causa da LGPD, não tem o nome de quem
 * registrou nem dos apoiadores: só o total de apoios.
 */
public class OcorrenciaResponseDTO {

    private Long id;
    private Categoria categoria;
    private String descricao;
    private String bairro;
    private Integer gravidade;
    private Integer pessoasAfetadas;
    private Boolean pertoServicoEssencial;
    private Boolean afetaAcessibilidade;
    private StatusOcorrencia status;
    private LocalDate dataAbertura;
    private LocalDate dataResolucao;
    private Integer totalApoios;
    private Integer pontos;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }

    public Integer getGravidade() { return gravidade; }
    public void setGravidade(Integer gravidade) { this.gravidade = gravidade; }

    public Integer getPessoasAfetadas() { return pessoasAfetadas; }
    public void setPessoasAfetadas(Integer pessoasAfetadas) { this.pessoasAfetadas = pessoasAfetadas; }

    public Boolean getPertoServicoEssencial() { return pertoServicoEssencial; }
    public void setPertoServicoEssencial(Boolean pertoServicoEssencial) { this.pertoServicoEssencial = pertoServicoEssencial; }

    public Boolean getAfetaAcessibilidade() { return afetaAcessibilidade; }
    public void setAfetaAcessibilidade(Boolean afetaAcessibilidade) { this.afetaAcessibilidade = afetaAcessibilidade; }

    public StatusOcorrencia getStatus() { return status; }
    public void setStatus(StatusOcorrencia status) { this.status = status; }

    public LocalDate getDataAbertura() { return dataAbertura; }
    public void setDataAbertura(LocalDate dataAbertura) { this.dataAbertura = dataAbertura; }

    public LocalDate getDataResolucao() { return dataResolucao; }
    public void setDataResolucao(LocalDate dataResolucao) { this.dataResolucao = dataResolucao; }

    public Integer getTotalApoios() { return totalApoios; }
    public void setTotalApoios(Integer totalApoios) { this.totalApoios = totalApoios; }

    public Integer getPontos() { return pontos; }
    public void setPontos(Integer pontos) { this.pontos = pontos; }
}
