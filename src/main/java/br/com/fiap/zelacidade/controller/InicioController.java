package br.com.fiap.zelacidade.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** GET / : mostra que a API está no ar e lista as rotas principais (útil para "acordar" o Render). */
@RestController
public class InicioController {

    @GetMapping("/")
    public Map<String, Object> inicio() {
        return Map.of(
                "api", "ZelaCidade - zeladoria colaborativa com fila de prioridade",
                "grupo", "INOVEX - FIAP - Global Solution 2026/2",
                "status", "no ar",
                "rotas", List.of("/ocorrencias", "/ocorrencias/{id}", "/ocorrencias/{id}/atualizacoes",
                        "/ocorrencias/{id}/apoios", "/atualizacoes/{id}", "/apoios/{id}", "/cidadaos"));
    }
}
