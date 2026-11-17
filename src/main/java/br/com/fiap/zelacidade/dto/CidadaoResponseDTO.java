package br.com.fiap.zelacidade.dto;

import java.time.LocalDate;

/** Cidadão nas rotas extras (/cidadaos), pensadas para a equipe da prefeitura. */
public class CidadaoResponseDTO {

    private Long id;
    private String nome;
    private LocalDate dataCadastro;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public LocalDate getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDate dataCadastro) { this.dataCadastro = dataCadastro; }
}
