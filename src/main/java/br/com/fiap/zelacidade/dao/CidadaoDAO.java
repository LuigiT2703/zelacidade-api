package br.com.fiap.zelacidade.dao;

import br.com.fiap.zelacidade.exception.AcessoDadosException;
import br.com.fiap.zelacidade.model.Cidadao;
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

/** Acesso à tabela tb_zc_cidadao com JDBC puro. */
@Repository
public class CidadaoDAO {

    private final DataSource dataSource;

    // O Spring entrega o DataSource (pool de conexões configurado no application.properties).
    public CidadaoDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Cidadao inserir(Cidadao cidadao) {
        String sql = "INSERT INTO tb_zc_cidadao (cd_cidadao, nm_cidadao, dt_cadastro) "
                + "VALUES (sq_tb_zc_cidadao.NEXTVAL, ?, ?)";
        // try-with-resources: fecha a conexão e o statement sozinho, mesmo se der erro
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql, new String[]{"cd_cidadao"})) {
            ps.setString(1, cidadao.getNome());
            ps.setDate(2, Date.valueOf(cidadao.getDataCadastro()));
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    cidadao.setId(chaves.getLong(1));
                }
            }
            return cidadao;
        } catch (SQLException e) {
            throw new AcessoDadosException("Erro ao inserir cidadão", e);
        }
    }

    public List<Cidadao> listar() {
        String sql = "SELECT cd_cidadao, nm_cidadao, dt_cadastro FROM tb_zc_cidadao ORDER BY cd_cidadao";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Cidadao> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(montar(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new AcessoDadosException("Erro ao listar cidadãos", e);
        }
    }

    public Optional<Cidadao> buscarPorId(Long id) {
        String sql = "SELECT cd_cidadao, nm_cidadao, dt_cadastro FROM tb_zc_cidadao WHERE cd_cidadao = ?";
        return buscarUm(sql, ps -> ps.setLong(1, id));
    }

    /** Busca pelo nome já padronizado (é UNIQUE na tabela). */
    public Optional<Cidadao> buscarPorNome(String nomePadronizado) {
        String sql = "SELECT cd_cidadao, nm_cidadao, dt_cadastro FROM tb_zc_cidadao WHERE nm_cidadao = ?";
        return buscarUm(sql, ps -> ps.setString(1, nomePadronizado));
    }

    public void atualizar(Cidadao cidadao) {
        String sql = "UPDATE tb_zc_cidadao SET nm_cidadao = ? WHERE cd_cidadao = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, cidadao.getNome());
            ps.setLong(2, cidadao.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AcessoDadosException("Erro ao atualizar cidadão", e);
        }
    }

    public void remover(Long id) {
        String sql = "DELETE FROM tb_zc_cidadao WHERE cd_cidadao = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AcessoDadosException("Erro ao remover cidadão", e);
        }
    }

    /** Quantas ocorrências e apoios estão ligados ao cidadão (os "filhos" que impedem a remoção). */
    public int contarVinculos(Long id) {
        String sql = "SELECT (SELECT COUNT(*) FROM tb_zc_ocorrencia WHERE cd_cidadao = ?) "
                + "+ (SELECT COUNT(*) FROM tb_zc_apoio WHERE cd_cidadao = ?) FROM dual";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new AcessoDadosException("Erro ao contar vínculos do cidadão", e);
        }
    }

    private Optional<Cidadao> buscarUm(String sql, Parametros parametros) {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            parametros.preencher(ps);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(montar(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new AcessoDadosException("Erro ao buscar cidadão", e);
        }
    }

    private Cidadao montar(ResultSet rs) throws SQLException {
        return new Cidadao(rs.getLong("cd_cidadao"), rs.getString("nm_cidadao"),
                rs.getDate("dt_cadastro").toLocalDate());
    }
}
