package br.com.fiap.zelacidade.service;

import br.com.fiap.zelacidade.dao.AtualizacaoDAO;
import br.com.fiap.zelacidade.dto.AtualizacaoEdicaoDTO;
import br.com.fiap.zelacidade.dto.AtualizacaoRequestDTO;
import br.com.fiap.zelacidade.dto.AtualizacaoResponseDTO;
import br.com.fiap.zelacidade.exception.RecursoNaoEncontradoException;
import br.com.fiap.zelacidade.exception.RequisicaoInvalidaException;
import br.com.fiap.zelacidade.model.Atualizacao;
import br.com.fiap.zelacidade.model.Ocorrencia;
import br.com.fiap.zelacidade.util.Datas;
import br.com.fiap.zelacidade.util.Texto;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/** Histórico de status (o lado N do 1:N Ocorrência -> Atualização). */
@Service
public class AtualizacaoService {

    private final AtualizacaoDAO atualizacaoDAO;
    private final OcorrenciaService ocorrenciaService;
    private final ModelMapper modelMapper;

    public AtualizacaoService(AtualizacaoDAO atualizacaoDAO, OcorrenciaService ocorrenciaService, ModelMapper modelMapper) {
        this.atualizacaoDAO = atualizacaoDAO;
        this.ocorrenciaService = ocorrenciaService;
        this.modelMapper = modelMapper;
    }

    public List<AtualizacaoResponseDTO> listarDaOcorrencia(Long ocorrenciaId) {
        ocorrenciaService.buscarModelo(ocorrenciaId);   // 404 se a ocorrência não existe
        return atualizacaoDAO.listarPorOcorrencia(ocorrenciaId).stream().map(this::paraResposta).toList();
    }

    /**
     * Registra uma linha no histórico e muda o status da ocorrência.
     * Regras: status igual ao atual é recusado (400); RESOLVIDA grava a data e reabrir apaga (RN06).
     */
    public AtualizacaoResponseDTO registrar(Long ocorrenciaId, AtualizacaoRequestDTO dto) {
        Ocorrencia ocorrencia = ocorrenciaService.buscarModelo(ocorrenciaId);
        if (ocorrencia.getStatus() == dto.getStatus()) {
            throw new RequisicaoInvalidaException("A ocorrência já está com o status " + dto.getStatus() + ".");
        }
        Atualizacao nova = modelMapper.map(dto, Atualizacao.class);
        nova.setOcorrenciaId(ocorrenciaId);
        limparTextos(nova);
        nova.setDataHora(Datas.agora());

        var dataResolucao = OcorrenciaService.novaDataResolucao(
                ocorrencia.getStatus(), dto.getStatus(), ocorrencia.getDataResolucao());
        return paraResposta(atualizacaoDAO.inserirMudandoStatus(nova, dataResolucao));
    }

    public AtualizacaoResponseDTO buscar(Long id) {
        return paraResposta(buscarModelo(id));
    }

    public AtualizacaoResponseDTO editar(Long id, AtualizacaoEdicaoDTO dto) {
        Atualizacao atualizacao = buscarModelo(id);
        atualizacao.setComentario(dto.getComentario());
        atualizacao.setResponsavel(dto.getResponsavel());
        limparTextos(atualizacao);
        atualizacaoDAO.atualizarTexto(atualizacao);
        return paraResposta(atualizacao);
    }

    public void remover(Long id) {
        buscarModelo(id);
        atualizacaoDAO.remover(id);
    }

    private Atualizacao buscarModelo(Long id) {
        return atualizacaoDAO.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Não encontramos a atualização #" + id + "."));
    }

    private void limparTextos(Atualizacao a) {
        a.setComentario(Texto.limpar(a.getComentario()));
        a.setResponsavel(Texto.limpar(a.getResponsavel()));
        if (a.getComentario().length() < 10) {
            throw new RequisicaoInvalidaException("O comentário deve ter de 10 a 300 caracteres.");
        }
        if (a.getResponsavel().length() < 3) {
            throw new RequisicaoInvalidaException("O responsável deve ter de 3 a 80 caracteres.");
        }
    }

    private AtualizacaoResponseDTO paraResposta(Atualizacao a) {
        return modelMapper.map(a, AtualizacaoResponseDTO.class);
    }
}
