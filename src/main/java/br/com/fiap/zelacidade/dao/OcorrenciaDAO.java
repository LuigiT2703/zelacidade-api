package br.com.fiap.zelacidade.dao;

import br.com.fiap.zelacidade.exception.AcessoDadosException;
import br.com.fiap.zelacidade.model.Categoria;
import br.com.fiap.zelacidade.model.Ocorrencia;
import br.com.fiap.zelacidade.model.StatusOcorrencia;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acesso à tabela tb_zc_ocorrencia com JDBC puro. */
@Repository
public class OcorrenciaDAO {

    // O total de apoios não é coluna: contamos na tb_zc_apoio com uma subconsulta.
    private static final String SELECT_BASE = "SELECT o.cd_ocorrencia, o.cd_cidadao, o.ds_categoria, o.ds_ocorrencia, "
            + "o.nm_bairro, o.nr_gravidade, o.nr_pessoas_afetadas, o.st_perto_servico, o.st_acessibilidade, "
            + "o.st_ocorrencia, o.dt_abertura, o.dt_resolucao, "
            + "(SELECT COUNT(*) FROM tb_zc_apoio a WHERE a.cd_ocorrencia = o.cd_ocorrencia) AS nr_total_apoios "
            + "FROM tb_zc_ocorrencia o ";

    private final DataSource dataSource;

    public OcorrenciaDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Ocorrencia inserir(Ocorrencia ocorrencia) {
        String sql = "INSERT INTO tb_zc_ocorrencia (cd_ocorrencia, cd_cidadao, ds_categoria, ds_ocorrencia, nm_bairro, "
                + "nr_gravidade, nr_pessoas_afetadas, st_perto_servico, st_acessibilidade, st_ocorrencia, dt_abertura, "
                + "dt_resolucao) VALUES (sq_tb_zc_ocorrencia.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql, new String[]{"cd_ocorrencia"})) {
            ps.setLong(1, ocorrencia.getCidadaoId());
            preencherCampos(ps, ocorrencia, 2);
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    ocorrencia.setId(chaves.getLong(1));
                }
            }
            return ocorrencia;
        } catch (SQLException e) {
            throw new AcessoDadosException("Erro ao inserir ocorrência", e);
        }
    }

    public List<Ocorrencia> listar() {
        return listarComFiltro("ORDER BY o.cd_ocorrencia", ps -> { });
    }

    /** Ocorrências ainda não resolvidas de uma categoria (usado na regra de duplicidade). */
    public List<Ocorrencia> listarPendentesPorCategoria(Categoria categoria) {
        return listarComFiltro("WHERE o.ds_categoria = ? AND o.st_ocorrencia <> 'RESOLVIDA' ORDER BY o.cd_ocorrencia",
                ps -> ps.setString(1, categoria.name()));
    }

    public Optional<Ocorrencia> buscarPorId(Long id) {
        List<Ocorrencia> lista = listarComFiltro("WHERE o.cd_ocorrencia = ?", ps -> ps.setLong(1, id));
        return lista.isEmpty() ? Optional.empty() : Optional.of(lista.get(0));
    }

    /** Atualiza os dados da ocorrência (não muda quem registrou nem a data de abertura). */
    public void atualizar(Ocorrencia ocorrencia) {
        String sql = "UPDATE tb_zc_ocorrencia SET ds_categoria = ?, ds_ocorrencia = ?, nm_bairro = ?, nr_gravidade = ?, "
                + "nr_pessoas_afetadas = ?, st_perto_servico = ?, st_acessibilidade = ?, st_ocorrencia = ?, "
                + "dt_abertura = ?, dt_resolucao = ? WHERE cd_ocorrencia = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            preencherCampos(ps, ocorrencia, 1);
            ps.setLong(11, ocorrencia.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AcessoDadosException("Erro ao atualizar ocorrência", e);
        }
    }

    /**
     * Remove a ocorrência junto com o histórico e os apoios dela, numa única transação:
     * ou apaga tudo, ou não apaga nada.
     */
    public void removerComDependentes(Long id) {
        try (Connection conexao = dataSource.getConnection()) {
            conexao.setAutoCommit(false);
            try (PreparedStatement apoios = conexao.prepareStatement("DELETE FROM tb_zc_apoio WHERE cd_ocorrencia = ?");
                 PreparedStatement historico = conexao.prepareStatement("DELETE FROM tb_zc_atualizacao WHERE cd_ocorrencia = ?");
                 PreparedStatement ocorrencia = conexao.prepareStatement("DELETE FROM tb_zc_ocorrencia WHERE cd_ocorrencia = ?")) {
                apoios.setLong(1, id);
                apoios.executeUpdate();
                historico.setLong(1, id);
                historico.executeUpdate();
                ocorrencia.setLong(1, id);
                ocorrencia.executeUpdate();
                conexao.commit();
            } catch (SQLException e) {
                conexao.rollback();
                throw e;
            } finally {
                conexao.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new AcessoDadosException("Erro ao remover ocorrência", e);
        }
    }

    private List<Ocorrencia> listarComFiltro(String complemento, Parametros parametros) {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(SELECT_BASE + complemento)) {
            parametros.preencher(ps);
            try (ResultSet rs = ps.executeQuery()) {
                List<Ocorrencia> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(montar(rs));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw new AcessoDadosException("Erro ao consultar ocorrências", e);
        }
    }

    /** Preenche os 10 campos comuns ao INSERT e ao UPDATE, a partir da posição "inicio". */
    private void preencherCampos(PreparedStatement ps, Ocorrencia o, int inicio) throws SQLException {
        ps.setString(inicio, o.getCategoria().name());
        ps.setString(inicio + 1, o.getDescricao());
        ps.setString(inicio + 2, o.getBairro());
        ps.setInt(inicio + 3, o.getGravidade());
        ps.setInt(inicio + 4, o.getPessoasAfetadas());
        ps.setString(inicio + 5, simNao(o.getPertoServicoEssencial()));
        ps.setString(inicio + 6, simNao(o.getAfetaAcessibilidade()));
        ps.setString(inicio + 7, o.getStatus().name());
        ps.setDate(inicio + 8, Date.valueOf(o.getDataAbertura()));
        if (o.getDataResolucao() == null) {
            ps.setNull(inicio + 9, Types.DATE);
        } else {
            ps.setDate(inicio + 9, Date.valueOf(o.getDataResolucao()));
        }
    }

    private Ocorrencia montar(ResultSet rs) throws SQLException {
        Ocorrencia o = new Ocorrencia();
        o.setId(rs.getLong("cd_ocorrencia"));
        o.setCidadaoId(rs.getLong("cd_cidadao"));
        o.setCategoria(Categoria.valueOf(rs.getString("ds_categoria")));
        o.setDescricao(rs.getString("ds_ocorrencia"));
        o.setBairro(rs.getString("nm_bairro"));
        o.setGravidade(rs.getInt("nr_gravidade"));
        o.setPessoasAfetadas(rs.getInt("nr_pessoas_afetadas"));
        o.setPertoServicoEssencial("S".equals(rs.getString("st_perto_servico")));
        o.setAfetaAcessibilidade("S".equals(rs.getString("st_acessibilidade")));
        o.setStatus(StatusOcorrencia.valueOf(rs.getString("st_ocorrencia")));
        o.setDataAbertura(rs.getDate("dt_abertura").toLocalDate());
        Date resolucao = rs.getDate("dt_resolucao");
        o.setDataResolucao(resolucao == null ? null : resolucao.toLocalDate());
        o.setTotalApoios(rs.getInt("nr_total_apoios"));
        return o;
    }

    // No Oracle os booleanos ficam como 'S' ou 'N' (CHAR(1) com CHECK)
    private String simNao(Boolean valor) {
        return Boolean.TRUE.equals(valor) ? "S" : "N";
    }
}
