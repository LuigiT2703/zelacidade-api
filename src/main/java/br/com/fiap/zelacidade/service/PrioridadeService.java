package br.com.fiap.zelacidade.service;

import br.com.fiap.zelacidade.model.Ocorrencia;
import br.com.fiap.zelacidade.model.StatusOcorrencia;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Regra pública de prioridade: a MESMA fórmula do motor em Python
 * (INOVEX_GS_Python/ocorrencias.py, função calcular_pontuacao).
 *
 * pontos = gravidade x 10
 *        + mín(pessoas afetadas / 10, 20)
 *        + mín(dias em aberto x 2, 20)
 *        + 15 se fica perto de escola, hospital ou posto
 *        + 10 se afeta a acessibilidade
 *        + mín(apoios x 3, 15)
 */
@Service
public class PrioridadeService {

    static final int PONTOS_POR_GRAVIDADE = 10;
    static final int LIMITE_PONTOS_PESSOAS = 20;
    static final int PONTOS_POR_DIA = 2;
    static final int LIMITE_PONTOS_DIAS = 20;
    static final int BONUS_SERVICO_ESSENCIAL = 15;
    static final int BONUS_ACESSIBILIDADE = 10;
    static final int PONTOS_POR_APOIO = 3;
    static final int LIMITE_PONTOS_APOIOS = 15;

    public int calcularPontos(Ocorrencia o, LocalDate hoje) {
        int pontos = o.getGravidade() * PONTOS_POR_GRAVIDADE;
        pontos += Math.min(o.getPessoasAfetadas() / 10, LIMITE_PONTOS_PESSOAS);
        pontos += Math.min(diasEmAberto(o, hoje) * PONTOS_POR_DIA, LIMITE_PONTOS_DIAS);
        if (Boolean.TRUE.equals(o.getPertoServicoEssencial())) {
            pontos += BONUS_SERVICO_ESSENCIAL;
        }
        if (Boolean.TRUE.equals(o.getAfetaAcessibilidade())) {
            pontos += BONUS_ACESSIBILIDADE;
        }
        pontos += Math.min(o.getTotalApoios() * PONTOS_POR_APOIO, LIMITE_PONTOS_APOIOS);
        return pontos;
    }

    /** Dias entre a abertura e hoje; se já foi resolvida, conta só até a resolução. Nunca negativo. */
    public int diasEmAberto(Ocorrencia o, LocalDate hoje) {
        LocalDate fim = hoje;
        if (o.getStatus() == StatusOcorrencia.RESOLVIDA && o.getDataResolucao() != null) {
            fim = o.getDataResolucao();
        }
        long dias = ChronoUnit.DAYS.between(o.getDataAbertura(), fim);
        return (int) Math.max(dias, 0);
    }
}
