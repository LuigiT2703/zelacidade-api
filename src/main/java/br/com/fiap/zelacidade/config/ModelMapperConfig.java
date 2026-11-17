package br.com.fiap.zelacidade.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Cria um único ModelMapper para a aplicação inteira (converte model <-> DTO). */
@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        // STRICT: só copia campos com o MESMO nome (ex.: "status" -> "status"), sem "adivinhar"
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return mapper;
    }
}
