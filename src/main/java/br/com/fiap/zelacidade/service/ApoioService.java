package br.com.fiap.zelacidade.service;

import br.com.fiap.zelacidade.dao.ApoioDAO;
import br.com.fiap.zelacidade.dto.ApoioRequestDTO;
import br.com.fiap.zelacidade.dto.ApoioResponseDTO;
import br.com.fiap.zelacidade.dto.ApoioResultadoDTO;
import br.com.fiap.zelacidade.exception.RecursoNaoEncontradoException;
import br.com.fiap.zelacidade.exception.RegraNegocioException;
import br.com.fiap.zelacidade.model.Apoio;
import br.com.fiap.zelacidade.model.Cidadao;
import br.com.fiap.zelacidade.model.Ocorrencia;
import br.com.fiap.zelacidade.model.StatusOcorrencia;
import br.com.fiap.zelacidade.util.Datas;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/** Apoio colaborativo: o morador confirma um problema em vez de registrar de novo (RN05). */
@Service
public class ApoioService {

    private final ApoioDAO apoioDAO;
    private final OcorrenciaService ocorrenciaService;
    private final CidadaoService cidadaoService;
    private final ModelMapper modelMapper;

    public ApoioService(ApoioDAO apoioDAO, OcorrenciaService ocorrenciaService,
                        CidadaoService cidadaoService, ModelMapper modelMapper) {
        this.apoioDAO = apoioDAO;
        this.ocorrenciaService = ocorrenciaService;
        this.cidadaoService = cidadaoService;
        this.modelMapper = modelMapper;
    }

    /**
     * RN05, nesta ordem:
     * 1) resolvida não recebe apoio; 2) quem registrou não apoia a própria;
     * 3) cada pessoa apoia uma vez só (também garantido pelo UNIQUE do banco).
     */
    public ApoioResultadoDTO apoiar(Long ocorrenciaId, ApoioRequestDTO dto) {
        Ocorrencia ocorrencia = ocorrenciaService.buscarModelo(ocorrenciaId);
        if (ocorrencia.getStatus() == StatusOcorrencia.RESOLVIDA) {
            throw new RegraNegocioException("Ocorrências resolvidas não recebem apoio.");
        }
        Cidadao cidadao = cidadaoService.buscarOuCriar(dto.getNome());
        conferirRegras(ocorrencia, cidadao);
        apoioDAO.inserir(new Apoio(ocorrenciaId, cidadao.getId(), Datas.hoje()));
        return new ApoioResultadoDTO(ocorrenciaId, apoioDAO.contarPorOcorrencia(ocorrenciaId));
    }

    public List<ApoioResponseDTO> listarDaOcorrencia(Long ocorrenciaId) {
        ocorrenciaService.buscarModelo(ocorrenciaId);
        return apoioDAO.listarPorOcorrencia(ocorrenciaId).stream().map(this::paraResposta).toList();
    }

    public ApoioResponseDTO buscar(Long id) {
        return paraResposta(buscarModelo(id));
    }

    /** Corrige o nome de quem apoiou (troca o cidadão), respeitando as mesmas regras. */
    public ApoioResponseDTO atualizar(Long id, ApoioRequestDTO dto) {
        Apoio apoio = buscarModelo(id);
        Ocorrencia ocorrencia = ocorrenciaService.buscarModelo(apoio.getOcorrenciaId());
        Cidadao cidadao = cidadaoService.buscarOuCriar(dto.getNome());
        if (!cidadao.getId().equals(apoio.getCidadaoId())) {
            conferirRegras(ocorrencia, cidadao);
            apoio.setCidadaoId(cidadao.getId());
            apoioDAO.atualizar(apoio);
        }
        return paraResposta(apoio);
    }

    public void remover(Long id) {
        buscarModelo(id);
        apoioDAO.remover(id);
    }

    private void conferirRegras(Ocorrencia ocorrencia, Cidadao cidadao) {
        if (cidadao.getId().equals(ocorrencia.getCidadaoId())) {
            throw new RegraNegocioException("Você registrou esta ocorrência, então seu apoio já está contado.");
        }
        if (apoioDAO.existe(ocorrencia.getId(), cidadao.getId())) {
            throw new RegraNegocioException("Você já apoiou esta ocorrência. Cada pessoa pode apoiar só uma vez.");
        }
    }

    private Apoio buscarModelo(Long id) {
        return apoioDAO.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Não encontramos o apoio #" + id + "."));
    }

    private ApoioResponseDTO paraResposta(Apoio apoio) {
        return modelMapper.map(apoio, ApoioResponseDTO.class);
    }
}
