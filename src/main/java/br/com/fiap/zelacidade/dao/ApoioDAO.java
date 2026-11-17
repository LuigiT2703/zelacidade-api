package br.com.fiap.zelacidade.dao;

import br.com.fiap.zelacidade.exception.AcessoDadosException;
import br.com.fiap.zelacidade.model.Apoio;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acesso à tabela associativa tb_zc_apoio (Ocorrência x Cidadão) com JDBC puro. */
@Repository
public class ApoioDAO {

    private static final String SELECT_BASE = "SELECT cd_apoio, cd_ocorrencia, cd_cidadao, dt_apoio FROM tb_zc_apoio ";

    private final DataSource dataSource;

    public ApoioDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Apoio inserir(Apoio apoio) {
        String sql = "INSERT INTO tb_zc_apoio (cd_apoio, cd_ocorrencia, cd_cidadao, dt_apoio) "
                + "VALUES (sq_tb_zc_apoio.NEXTVAL, ?, ?, ?)";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql, new String[]{"cd_apoio"})) {
            ps.setLong(1, apoio.getOcorrenciaId());
            ps.setLong(2, apoio.getCidadaoId());
            ps.setDate(3, Date.valueOf(apoio.getDataApoio()));
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    apoio.setId(chaves.getLong(1));
                }
            }
            return apoio;
        } catch (SQLException e) {
            throw new AcessoDadosException("Erro ao inserir apoio", e);
        }
    }

    public List<Apoio> listarPorOcorrencia(Long ocorrenciaId) {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(SELECT_BASE + "WHERE cd_ocorrencia = ? ORDER BY cd_apoio")) {
            ps.setLong(1, ocorrenciaId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Apoio> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(montar(rs));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw new AcessoDadosException("Erro ao listar apoios", e);
        }
    }

    public Optional<Apoio> buscarPorId(Long id) {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(SELECT_BASE + "WHERE cd_apoio = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(montar(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new AcessoDadosException("Erro ao buscar apoio", e);
        }
    }

    /** true se esse cidadão já apoiou essa ocorrência (RN05). */
    public boolean existe(Long ocorrenciaId, Long cidadaoId) {
        String sql = "SELECT COUNT(*) FROM tb_zc_apoio WHERE cd_ocorrencia = ? AND cd_cidadao = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, ocorrenciaId);
            ps.setLong(2, cidadaoId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new AcessoDadosException("Erro ao conferir apoio", e);
        }
    }

    public int contarPorOcorrencia(Long ocorrenciaId) {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement("SELECT COUNT(*) FROM tb_zc_apoio WHERE cd_ocorrencia = ?")) {
            ps.setLong(1, ocorrenciaId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new AcessoDadosException("Erro ao contar apoios", e);
        }
    }

    /** Troca o cidadão de um apoio (corrigir um nome digitado errado). */
    public void atualizar(Apoio apoio) {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement("UPDATE tb_zc_apoio SET cd_cidadao = ? WHERE cd_apoio = ?")) {
            ps.setLong(1, apoio.getCidadaoId());
            ps.setLong(2, apoio.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AcessoDadosException("Erro ao atualizar apoio", e);
        }
    }

    public void remover(Long id) {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement("DELETE FROM tb_zc_apoio WHERE cd_apoio = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AcessoDadosException("Erro ao remover apoio", e);
        }
    }

    private Apoio montar(ResultSet rs) throws SQLException {
        Apoio a = new Apoio(rs.getLong("cd_ocorrencia"), rs.getLong("cd_cidadao"), rs.getDate("dt_apoio").toLocalDate());
        a.setId(rs.getLong("cd_apoio"));
        return a;
    }
}
