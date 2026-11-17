package br.com.fiap.zelacidade.dto;

/** Corpo padrão de erro do contrato: { "status": 400, "mensagem": "..." }. */
public record ErroDTO(int status, String mensagem) {
}
