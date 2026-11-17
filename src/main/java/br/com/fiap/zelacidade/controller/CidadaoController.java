package br.com.fiap.zelacidade.controller;

import br.com.fiap.zelacidade.dto.CidadaoRequestDTO;
import br.com.fiap.zelacidade.dto.CidadaoResponseDTO;
import br.com.fiap.zelacidade.service.CidadaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/** CRUD de cidadãos (rotas extras, para a equipe da prefeitura). */
@RestController
@RequestMapping("/cidadaos")
public class CidadaoController {

    private final CidadaoService cidadaoService;

    public CidadaoController(CidadaoService cidadaoService) {
        this.cidadaoService = cidadaoService;
    }

    @GetMapping
    public List<CidadaoResponseDTO> listar() {
        return cidadaoService.listar();
    }

    @GetMapping("/{id}")
    public CidadaoResponseDTO buscar(@PathVariable Long id) {
        return cidadaoService.buscar(id);
    }

    @PostMapping
    public ResponseEntity<CidadaoResponseDTO> cadastrar(@Valid @RequestBody CidadaoRequestDTO dto) {
        CidadaoResponseDTO criado = cidadaoService.cadastrar(dto);
        return ResponseEntity.created(URI.create("/cidadaos/" + criado.getId())).body(criado);
    }

    @PutMapping("/{id}")
    public CidadaoResponseDTO atualizar(@PathVariable Long id, @Valid @RequestBody CidadaoRequestDTO dto) {
        return cidadaoService.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        cidadaoService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
