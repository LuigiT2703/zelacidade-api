package br.com.fiap.zelacidade.util;

import java.text.Normalizer;

/** Ajudantes de texto usados nas regras de negócio. */
public final class Texto {

    private Texto() {
    }

    /** Tira espaços do começo e do fim e troca espaços repetidos por um só. */
    public static String limpar(String texto) {
        if (texto == null) {
            return null;
        }
        return texto.trim().replaceAll("\\s+", " ");
    }

    /** Padroniza o nome da pessoa: "  Ana   LIMA " vira "ana lima" (RN05). */
    public static String padronizarNome(String nome) {
        String limpo = limpar(nome);
        return limpo == null ? null : limpo.toLowerCase();
    }

    /** Versão para comparar textos sem acento e sem maiúsculas: "Capão Redondo" vira "capao redondo". */
    public static String paraComparar(String texto) {
        String limpo = padronizarNome(texto);
        if (limpo == null) {
            return "";
        }
        String semAcento = Normalizer.normalize(limpo, Normalizer.Form.NFD);
        return semAcento.replaceAll("\\p{M}", "");
    }
}
