package br.com.fiap.zelacidade.controller;

import br.com.fiap.zelacidade.dto.ApoioRequestDTO;
import br.com.fiap.zelacidade.dto.ApoioResponseDTO;
import br.com.fiap.zelacidade.dto.ApoioResultadoDTO;
import br.com.fiap.zelacidade.dto.AtualizacaoRequestDTO;
import br.com.fiap.zelacidade.dto.AtualizacaoResponseDTO;
import br.com.fiap.zelacidade.dto.OcorrenciaRequestDTO;
import br.com.fiap.zelacidade.dto.OcorrenciaResponseDTO;
import br.com.fiap.zelacidade.dto.OcorrenciaUpdateDTO;
import br.com.fiap.zelacidade.service.ApoioService;
import br.com.fiap.zelacidade.service.AtualizacaoService;
import br.com.fiap.zelacidade.service.OcorrenciaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

/**
 * Rotas de /ocorrencias, exatamente como no CONTRATO_API.md, mais as rotas
 * aninhadas do 1:N (/ocorrencias/{id}/atualizacoes) e dos apoios.
 * O controller só recebe, valida (@Valid) e devolve: as regras ficam nos services.
 */
@RestController
@RequestMapping("/ocorrencias")
public class OcorrenciaController {

    private final OcorrenciaService ocorrenciaService;
    private final AtualizacaoService atualizacaoService;
    private final ApoioService apoioService;

    public OcorrenciaController(OcorrenciaService ocorrenciaService, AtualizacaoService atualizacaoService,
                                ApoioService apoioService) {
        this.ocorrenciaService = ocorrenciaService;
        this.atualizacaoService = atualizacaoService;
        this.apoioService = apoioService;
    }

    @GetMapping
    public List<OcorrenciaResponseDTO> listar() {
        return ocorrenciaService.listar();
    }

    @GetMapping("/{id}")
    public OcorrenciaResponseDTO buscar(@PathVariable Long id) {
        return ocorrenciaService.buscar(id);
    }

    /** 201 Created + header Location apontando para a ocorrência nova. */
    @PostMapping
    public ResponseEntity<OcorrenciaResponseDTO> cadastrar(@Valid @RequestBody OcorrenciaRequestDTO dto) {
        OcorrenciaResponseDTO criada = ocorrenciaService.cadastrar(dto);
        return ResponseEntity.created(URI.create("/ocorrencias/" + criada.getId())).body(criada);
    }

    @PutMapping("/{id}")
    public OcorrenciaResponseDTO atualizar(@PathVariable Long id, @Valid @RequestBody OcorrenciaUpdateDTO dto) {
        return ocorrenciaService.atualizar(id, dto);
    }

    /** 204 No Content: deu certo e não há corpo para devolver. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        ocorrenciaService.remover(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- 1:N: histórico de status ----------

    @GetMapping("/{id}/atualizacoes")
    public List<AtualizacaoResponseDTO> listarAtualizacoes(@PathVariable Long id) {
        return atualizacaoService.listarDaOcorrencia(id);
    }

    @PostMapping("/{id}/atualizacoes")
    public ResponseEntity<AtualizacaoResponseDTO> registrarAtualizacao(@PathVariable Long id,
                                                                       @Valid @RequestBody AtualizacaoRequestDTO dto) {
        AtualizacaoResponseDTO criada = atualizacaoService.registrar(id, dto);
        return ResponseEntity.created(URI.create("/atualizacoes/" + criada.getId())).body(criada);
    }

    // ---------- apoios (associativa Ocorrência x Cidadão) ----------

    @PostMapping("/{id}/apoios")
    public ResponseEntity<ApoioResultadoDTO> apoiar(@PathVariable Long id, @Valid @RequestBody ApoioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(apoioService.apoiar(id, dto));
    }

    @GetMapping("/{id}/apoios")
    public List<ApoioResponseDTO> listarApoios(@PathVariable Long id) {
        return apoioService.listarDaOcorrencia(id);
    }
}
