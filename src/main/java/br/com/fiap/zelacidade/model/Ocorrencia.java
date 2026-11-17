package br.com.fiap.zelacidade.model;

import java.time.LocalDate;

/**
 * Problema do espaço público relatado por um cidadão.
 * Relacionamentos: pertence a um Cidadao (quem registrou, cidadaoId) e tem
 * várias Atualizacao (1:N, o histórico) e vários Apoio.
 */
public class Ocorrencia {

    private Long id;
    private Long cidadaoId;
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
    /** Não é coluna: o DAO conta os apoios na tabela tb_zc_apoio. */
    private int totalApoios;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCidadaoId() { return cidadaoId; }
    public void setCidadaoId(Long cidadaoId) { this.cidadaoId = cidadaoId; }

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

    public int getTotalApoios() { return totalApoios; }
    public void setTotalApoios(int totalApoios) { this.totalApoios = totalApoios; }
}
