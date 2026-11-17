package br.com.fiap.zelacidade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API REST do ZelaCidade: zeladoria colaborativa com fila de prioridade.
 * Global Solution 2026/2 (CitizenTech) - Grupo INOVEX - FIAP.
 */
@SpringBootApplication
public class ZelaCidadeApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZelaCidadeApiApplication.class, args);
    }
}
