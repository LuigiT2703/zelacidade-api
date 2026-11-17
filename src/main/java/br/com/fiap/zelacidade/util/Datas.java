package br.com.fiap.zelacidade.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/**
 * Datas sempre no fuso de São Paulo. O servidor do Render roda em UTC:
 * sem isso, uma ocorrência registrada às 22h sairia com a data do dia seguinte.
 */
public final class Datas {

    public static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    private Datas() {
    }

    public static LocalDate hoje() {
        return LocalDate.now(FUSO);
    }

    public static LocalDateTime agora() {
        return LocalDateTime.now(FUSO).truncatedTo(ChronoUnit.SECONDS);
    }
}
