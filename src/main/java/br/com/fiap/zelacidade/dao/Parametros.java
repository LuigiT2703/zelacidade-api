package br.com.fiap.zelacidade.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/** Pequena interface para os DAOs preencherem os "?" de um PreparedStatement. */
@FunctionalInterface
interface Parametros {
    void preencher(PreparedStatement ps) throws SQLException;
}
