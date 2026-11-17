package br.com.fiap.zelacidade.service;

import br.com.fiap.zelacidade.dao.CidadaoDAO;
import br.com.fiap.zelacidade.dto.CidadaoRequestDTO;
import br.com.fiap.zelacidade.dto.CidadaoResponseDTO;
import br.com.fiap.zelacidade.exception.RecursoNaoEncontradoException;
import br.com.fiap.zelacidade.exception.RegraNegocioException;
import br.com.fiap.zelacidade.exception.RequisicaoInvalidaException;
import br.com.fiap.zelacidade.model.Cidadao;
import br.com.fiap.zelacidade.util.Datas;
import br.com.fiap.zelacidade.util.Texto;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/** Regras do cadastro de cidadãos (rotas extras /cidadaos). */
@Service
public class CidadaoService {

    private final CidadaoDAO cidadaoDAO;
    private final ModelMapper modelMapper;

    public CidadaoService(CidadaoDAO cidadaoDAO, ModelMapper modelMapper) {
        this.cidadaoDAO = cidadaoDAO;
        this.modelMapper = modelMapper;
    }

    public List<CidadaoResponseDTO> listar() {
        return cidadaoDAO.listar().stream().map(this::paraResposta).toList();
    }

    public CidadaoResponseDTO buscar(Long id) {
        return paraResposta(buscarModelo(id));
    }

    public CidadaoResponseDTO cadastrar(CidadaoRequestDTO dto) {
        String nome = Texto.padronizarNome(dto.getNome());
        validarTamanho(nome);
        if (cidadaoDAO.buscarPorNome(nome).isPresent()) {
            throw new RegraNegocioException("Já existe um cidadão cadastrado com esse nome.");
        }
        return paraResposta(cidadaoDAO.inserir(new Cidadao(null, nome, Datas.hoje())));
    }

    public CidadaoResponseDTO atualizar(Long id, CidadaoRequestDTO dto) {
        Cidadao cidadao = buscarModelo(id);
        String nome = Texto.padronizarNome(dto.getNome());
        validarTamanho(nome);
        cidadaoDAO.buscarPorNome(nome).ifPresent(outro -> {
            if (!outro.getId().equals(id)) {
                throw new RegraNegocioException("Já existe um cidadão cadastrado com esse nome.");
            }
        });
        cidadao.setNome(nome);
        cidadaoDAO.atualizar(cidadao);
        return paraResposta(cidadao);
    }

    /** Não remove quem tem ocorrências ou apoios (os "filhos"): a remoção quebraria o histórico. */
    public void remover(Long id) {
        buscarModelo(id);
        if (cidadaoDAO.contarVinculos(id) > 0) {
            throw new RegraNegocioException(
                    "Este cidadão tem ocorrências ou apoios registrados e não pode ser removido.");
        }
        cidadaoDAO.remover(id);
    }

    /**
     * Acha o cidadão pelo nome padronizado ou cria um novo. Usado quando alguém
     * registra ou apoia uma ocorrência: a pessoa só digita o nome.
     */
    public Cidadao buscarOuCriar(String nomeDigitado) {
        String nome = Texto.padronizarNome(nomeDigitado);
        validarTamanho(nome);
        return cidadaoDAO.buscarPorNome(nome)
                .orElseGet(() -> cidadaoDAO.inserir(new Cidadao(null, nome, Datas.hoje())));
    }

    private Cidadao buscarModelo(Long id) {
        return cidadaoDAO.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Não encontramos o cidadão #" + id + "."));
    }

    // "  a  " passa no @Size(min = 2) mas vira "a" depois de limpar: conferimos de novo.
    private void validarTamanho(String nome) {
        if (nome == null || nome.length() < 2) {
            throw new RequisicaoInvalidaException(
                    "O nome precisa ter pelo menos 2 letras.");
        }
    }

    private CidadaoResponseDTO paraResposta(Cidadao cidadao) {
        return modelMapper.map(cidadao, CidadaoResponseDTO.class);
    }
}
