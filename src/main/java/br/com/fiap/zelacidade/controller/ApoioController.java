package br.com.fiap.zelacidade.controller;

import br.com.fiap.zelacidade.dto.ApoioRequestDTO;
import br.com.fiap.zelacidade.dto.ApoioResponseDTO;
import br.com.fiap.zelacidade.service.ApoioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Rotas extras de um apoio. Apoiar e listar ficam em /ocorrencias/{id}/apoios. */
@RestController
@RequestMapping("/apoios")
public class ApoioController {

    private final ApoioService apoioService;

    public ApoioController(ApoioService apoioService) {
        this.apoioService = apoioService;
    }

    @GetMapping("/{id}")
    public ApoioResponseDTO buscar(@PathVariable Long id) {
        return apoioService.buscar(id);
    }

    @PutMapping("/{id}")
    public ApoioResponseDTO atualizar(@PathVariable Long id, @Valid @RequestBody ApoioRequestDTO dto) {
        return apoioService.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        apoioService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
