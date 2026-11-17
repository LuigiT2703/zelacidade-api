package br.com.fiap.zelacidade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Corpo do POST e do PUT /cidadaos (rotas extras). */
public class CidadaoRequestDTO {

    @NotBlank(message = "Informe o nome.")
    @Size(min = 2, max = 80, message = "O nome deve ter de 2 a 80 caracteres.")
    private String nome;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}
