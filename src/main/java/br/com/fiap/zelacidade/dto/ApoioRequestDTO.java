package br.com.fiap.zelacidade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Corpo do POST /ocorrencias/{id}/apoios e do PUT /apoios/{id}: { "nome": "Ana Lima" }. */
public class ApoioRequestDTO {

    @NotBlank(message = "Digite seu nome para apoiar.")
    @Size(min = 2, max = 80, message = "O nome deve ter de 2 a 80 caracteres.")
    private String nome;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}
