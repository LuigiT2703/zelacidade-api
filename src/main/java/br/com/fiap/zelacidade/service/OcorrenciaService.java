package br.com.fiap.zelacidade.service;

import br.com.fiap.zelacidade.dao.OcorrenciaDAO;
import br.com.fiap.zelacidade.dto.OcorrenciaRequestDTO;
import br.com.fiap.zelacidade.dto.OcorrenciaResponseDTO;
import br.com.fiap.zelacidade.dto.OcorrenciaUpdateDTO;
import br.com.fiap.zelacidade.exception.RecursoNaoEncontradoException;
import br.com.fiap.zelacidade.exception.RegraNegocioException;
import br.com.fiap.zelacidade.exception.RequisicaoInvalidaException;
import br.com.fiap.zelacidade.model.Categoria;
import br.com.fiap.zelacidade.model.Cidadao;
import br.com.fiap.zelacidade.model.Ocorrencia;
import br.com.fiap.zelacidade.model.StatusOcorrencia;
import br.com.fiap.zelacidade.util.Datas;
import br.com.fiap.zelacidade.util.Texto;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/** Regras de negócio das ocorrências. */
@Service
public class OcorrenciaService {

    private final OcorrenciaDAO ocorrenciaDAO;
    private final CidadaoService cidadaoService;
    private final PrioridadeService prioridadeService;
    private final ModelMapper modelMapper;

    public OcorrenciaService(OcorrenciaDAO ocorrenciaDAO, CidadaoService cidadaoService,
                             PrioridadeService prioridadeService, ModelMapper modelMapper) {
        this.ocorrenciaDAO = ocorrenciaDAO;
        this.cidadaoService = cidadaoService;
        this.prioridadeService = prioridadeService;
        this.modelMapper = modelMapper;
    }

    public List<OcorrenciaResponseDTO> listar() {
        LocalDate hoje = Datas.hoje();
        return ocorrenciaDAO.listar().stream().map(o -> paraResposta(o, hoje)).toList();
    }

    public OcorrenciaResponseDTO buscar(Long id) {
        return paraResposta(buscarModelo(id), Datas.hoje());
    }

    public OcorrenciaResponseDTO cadastrar(OcorrenciaRequestDTO dto) {
        // DTO -> model com o ModelMapper; depois aplicamos as regras do servidor
        Ocorrencia nova = modelMapper.map(dto, Ocorrencia.class);
        limparTextos(nova);
        aplicarRegraCalcada(nova);
        impedirDuplicada(nova, null);

        Cidadao autor = cidadaoService.buscarOuCriar(dto.getNomeCidadao());
        nova.setCidadaoId(autor.getId());
        nova.setStatus(StatusOcorrencia.ABERTA);   // sempre começa aberta
        nova.setDataAbertura(Datas.hoje());        // a data é do servidor, não do cliente
        nova.setDataResolucao(null);
        nova.setTotalApoios(0);
        return paraResposta(ocorrenciaDAO.inserir(nova), Datas.hoje());
    }

    public OcorrenciaResponseDTO atualizar(Long id, OcorrenciaUpdateDTO dto) {
        Ocorrencia atual = buscarModelo(id);
        StatusOcorrencia statusAnterior = atual.getStatus();

        modelMapper.map(dto, atual);   // copia os campos do DTO por cima do que veio do banco
        limparTextos(atual);
        aplicarRegraCalcada(atual);
        impedirDuplicada(atual, id);
        atual.setDataResolucao(novaDataResolucao(statusAnterior, atual.getStatus(), atual.getDataResolucao()));
        ocorrenciaDAO.atualizar(atual);
        return paraResposta(atual, Datas.hoje());
    }

    /** RN07: só remove duplicada ou engano. Resolvida é histórico e não pode ser excluída. */
    public void remover(Long id) {
        Ocorrencia ocorrencia = buscarModelo(id);
        if (ocorrencia.getStatus() == StatusOcorrencia.RESOLVIDA) {
            throw new RegraNegocioException(
                    "Ocorrências resolvidas fazem parte do histórico e não podem ser excluídas.");
        }
        ocorrenciaDAO.removerComDependentes(id);
    }

    /** Usado pelos outros services: devolve o model ou lança 404. */
    public Ocorrencia buscarModelo(Long id) {
        return ocorrenciaDAO.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Não encontramos a ocorrência #" + id + "."));
    }

    /**
     * RN06: ao resolver, grava a data de hoje; ao reabrir, apaga a data.
     * Se continuar resolvida, mantém a data que já existia.
     */
    public static LocalDate novaDataResolucao(StatusOcorrencia anterior, StatusOcorrencia novo, LocalDate dataAtual) {
        if (novo != StatusOcorrencia.RESOLVIDA) {
            return null;
        }
        if (anterior == StatusOcorrencia.RESOLVIDA && dataAtual != null) {
            return dataAtual;
        }
        return Datas.hoje();
    }

    /** RN04: problema de calçada sempre conta como acessibilidade. */
    private void aplicarRegraCalcada(Ocorrencia o) {
        if (o.getCategoria() == Categoria.CALCADA_ACESSIBILIDADE) {
            o.setAfetaAcessibilidade(true);
        }
    }

    /**
     * RN11: não aceita uma ocorrência igual a outra ainda pendente (mesma categoria,
     * bairro e descrição, sem diferenciar acento e maiúsculas). O certo é apoiar a existente.
     */
    private void impedirDuplicada(Ocorrencia o, Long idIgnorado) {
        String bairro = Texto.paraComparar(o.getBairro());
        String descricao = Texto.paraComparar(o.getDescricao());
        for (Ocorrencia existente : ocorrenciaDAO.listarPendentesPorCategoria(o.getCategoria())) {
            boolean mesma = existente.getId().equals(idIgnorado);
            if (!mesma && Texto.paraComparar(existente.getBairro()).equals(bairro)
                    && Texto.paraComparar(existente.getDescricao()).equals(descricao)) {
                throw new RegraNegocioException("Já existe uma ocorrência igual em aberto (#" + existente.getId()
                        + "). Apoie a ocorrência existente em vez de registrar de novo.");
            }
        }
    }

    // O @Size conta espaços; depois de limpar o texto conferimos o tamanho mínimo de novo.
    private void limparTextos(Ocorrencia o) {
        o.setDescricao(Texto.limpar(o.getDescricao()));
        o.setBairro(Texto.limpar(o.getBairro()));
        if (o.getDescricao().length() < 10) {
            throw new RequisicaoInvalidaException("A descrição deve ter de 10 a 300 caracteres.");
        }
        if (o.getBairro().length() < 3) {
            throw new RequisicaoInvalidaException("O bairro deve ter de 3 a 60 caracteres.");
        }
    }

    /** Model -> DTO de resposta, já com os pontos calculados. */
    public OcorrenciaResponseDTO paraResposta(Ocorrencia o, LocalDate hoje) {
        OcorrenciaResponseDTO resposta = modelMapper.map(o, OcorrenciaResponseDTO.class);
        resposta.setPontos(prioridadeService.calcularPontos(o, hoje));
        return resposta;
    }
}
