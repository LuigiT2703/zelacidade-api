package br.com.fiap.zelacidade.controller;

import br.com.fiap.zelacidade.dto.AtualizacaoEdicaoDTO;
import br.com.fiap.zelacidade.dto.AtualizacaoResponseDTO;
import br.com.fiap.zelacidade.service.AtualizacaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Rotas extras de uma linha do histórico. Listar e cadastrar ficam aninhados em /ocorrencias/{id}/atualizacoes. */
@RestController
@RequestMapping("/atualizacoes")
public class AtualizacaoController {

    private final AtualizacaoService atualizacaoService;

    public AtualizacaoController(AtualizacaoService atualizacaoService) {
        this.atualizacaoService = atualizacaoService;
    }

    @GetMapping("/{id}")
    public AtualizacaoResponseDTO buscar(@PathVariable Long id) {
        return atualizacaoService.buscar(id);
    }

    @PutMapping("/{id}")
    public AtualizacaoResponseDTO editar(@PathVariable Long id, @Valid @RequestBody AtualizacaoEdicaoDTO dto) {
        return atualizacaoService.editar(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        atualizacaoService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
