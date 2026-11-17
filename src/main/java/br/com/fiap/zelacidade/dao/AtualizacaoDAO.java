package br.com.fiap.zelacidade.dao;

import br.com.fiap.zelacidade.exception.AcessoDadosException;
import br.com.fiap.zelacidade.model.Atualizacao;
import br.com.fiap.zelacidade.model.StatusOcorrencia;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acesso à tabela tb_zc_atualizacao (histórico, lado N do 1:N) com JDBC puro. */
@Repository
public class AtualizacaoDAO {

    private static final String SELECT_BASE = "SELECT cd_atualizacao, cd_ocorrencia, st_atualizacao, ds_comentario, "
            + "nm_responsavel, dt_atualizacao FROM tb_zc_atualizacao ";

    private final DataSource dataSource;

    public AtualizacaoDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Grava a linha do histórico E muda o status da ocorrência na mesma transação.
     * Se uma das duas falhar, o rollback desfaz a outra (o status nunca muda "pela metade").
     */
    public Atualizacao inserirMudandoStatus(Atualizacao atualizacao, LocalDate novaDataResolucao) {
        String sqlHistorico = "INSERT INTO tb_zc_atualizacao (cd_atualizacao, cd_ocorrencia, st_atualizacao, ds_comentario, "
                + "nm_responsavel, dt_atualizacao) VALUES (sq_tb_zc_atualizacao.NEXTVAL, ?, ?, ?, ?, ?)";
        String sqlOcorrencia = "UPDATE tb_zc_ocorrencia SET st_ocorrencia = ?, dt_resolucao = ? WHERE cd_ocorrencia = ?";
        try (Connection conexao = dataSource.getConnection()) {
            conexao.setAutoCommit(false);
            try (PreparedStatement psHistorico = conexao.prepareStatement(sqlHistorico, new String[]{"cd_atualizacao"});
                 PreparedStatement psOcorrencia = conexao.prepareStatement(sqlOcorrencia)) {
                psHistorico.setLong(1, atualizacao.getOcorrenciaId());
                psHistorico.setString(2, atualizacao.getStatus().name());
                psHistorico.setString(3, atualizacao.getComentario());
                psHistorico.setString(4, atualizacao.getResponsavel());
                psHistorico.setTimestamp(5, Timestamp.valueOf(atualizacao.getDataHora()));
                psHistorico.executeUpdate();
                try (ResultSet chaves = psHistorico.getGeneratedKeys()) {
                    if (chaves.next()) {
                        atualizacao.setId(chaves.getLong(1));
                    }
                }
                psOcorrencia.setString(1, atualizacao.getStatus().name());
                if (novaDataResolucao == null) {
                    psOcorrencia.setNull(2, Types.DATE);
                } else {
                    psOcorrencia.setDate(2, Date.valueOf(novaDataResolucao));
                }
                psOcorrencia.setLong(3, atualizacao.getOcorrenciaId());
                psOcorrencia.executeUpdate();
                conexao.commit();
                return atualizacao;
            } catch (SQLException e) {
                conexao.rollback();
                throw e;
            } finally {
                conexao.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new AcessoDadosException("Erro ao registrar atualização", e);
        }
    }

    public List<Atualizacao> listarPorOcorrencia(Long ocorrenciaId) {
        String sql = SELECT_BASE + "WHERE cd_ocorrencia = ? ORDER BY dt_atualizacao DESC, cd_atualizacao DESC";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, ocorrenciaId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Atualizacao> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(montar(rs));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw new AcessoDadosException("Erro ao listar atualizações", e);
        }
    }

    public Optional<Atualizacao> buscarPorId(Long id) {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(SELECT_BASE + "WHERE cd_atualizacao = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(montar(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new AcessoDadosException("Erro ao buscar atualização", e);
        }
    }

    /** Corrige o texto de uma linha do histórico (o status e a data não mudam). */
    public void atualizarTexto(Atualizacao atualizacao) {
        String sql = "UPDATE tb_zc_atualizacao SET ds_comentario = ?, nm_responsavel = ? WHERE cd_atualizacao = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, atualizacao.getComentario());
            ps.setString(2, atualizacao.getResponsavel());
            ps.setLong(3, atualizacao.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AcessoDadosException("Erro ao atualizar a linha do histórico", e);
        }
    }

    public void remover(Long id) {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement("DELETE FROM tb_zc_atualizacao WHERE cd_atualizacao = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AcessoDadosException("Erro ao remover atualização", e);
        }
    }

    private Atualizacao montar(ResultSet rs) throws SQLException {
        Atualizacao a = new Atualizacao();
        a.setId(rs.getLong("cd_atualizacao"));
        a.setOcorrenciaId(rs.getLong("cd_ocorrencia"));
        a.setStatus(StatusOcorrencia.valueOf(rs.getString("st_atualizacao")));
        a.setComentario(rs.getString("ds_comentario"));
        a.setResponsavel(rs.getString("nm_responsavel"));
        a.setDataHora(rs.getTimestamp("dt_atualizacao").toLocalDateTime());
        return a;
    }
}
