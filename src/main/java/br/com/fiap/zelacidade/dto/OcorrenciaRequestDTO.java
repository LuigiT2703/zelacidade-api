package br.com.fiap.zelacidade.dto;

import br.com.fiap.zelacidade.model.Categoria;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Corpo do POST /ocorrencias ("NovaOcorrencia" no contrato). As faixas são as do CONTRATO_API.md. */
public class OcorrenciaRequestDTO {

    @NotNull(message = "Escolha a categoria do problema.")
    private Categoria categoria;

    @NotBlank(message = "Descreva o problema e o local.")
    @Size(min = 10, max = 300, message = "A descrição deve ter de 10 a 300 caracteres.")
    private String descricao;

    @NotBlank(message = "Informe o bairro.")
    @Size(min = 3, max = 60, message = "O bairro deve ter de 3 a 60 caracteres.")
    private String bairro;

    @NotNull(message = "Informe a gravidade de 1 a 5.")
    @Min(value = 1, message = "A gravidade deve ser um número de 1 a 5.")
    @Max(value = 5, message = "A gravidade deve ser um número de 1 a 5.")
    private Integer gravidade;

    @NotNull(message = "Informe quantas pessoas são afetadas por dia.")
    @Min(value = 1, message = "As pessoas afetadas devem ser de 1 a 100.000.")
    @Max(value = 100000, message = "As pessoas afetadas devem ser de 1 a 100.000.")
    private Integer pessoasAfetadas;

    @NotNull(message = "Informe se fica perto de escola, hospital ou posto de saúde (true ou false).")
    private Boolean pertoServicoEssencial;

    @NotNull(message = "Informe se o problema afeta a acessibilidade (true ou false).")
    private Boolean afetaAcessibilidade;

    @NotBlank(message = "Informe o seu nome.")
    @Size(min = 2, max = 80, message = "O nome deve ter de 2 a 80 caracteres.")
    private String nomeCidadao;

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

    public String getNomeCidadao() { return nomeCidadao; }
    public void setNomeCidadao(String nomeCidadao) { this.nomeCidadao = nomeCidadao; }
}
