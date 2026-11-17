package br.com.fiap.zelacidade.model;

import java.time.LocalDate;

/** Pessoa que registra ou apoia ocorrências. O nome é guardado padronizado (minúsculas, sem espaços extras). */
public class Cidadao {

    private Long id;
    private String nome;
    private LocalDate dataCadastro;

    public Cidadao() {
    }

    public Cidadao(Long id, String nome, LocalDate dataCadastro) {
        this.id = id;
        this.nome = nome;
        this.dataCadastro = dataCadastro;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public LocalDate getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDate dataCadastro) { this.dataCadastro = dataCadastro; }
}
