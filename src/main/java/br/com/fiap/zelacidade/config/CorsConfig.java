package br.com.fiap.zelacidade.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * CORS: diz ao navegador quais sites podem chamar esta API.
 * Liberamos o front local (Vite: 5173 no dev e 4173 no preview), os sites publicados na Vercel
 * (*.vercel.app) e qualquer outra URL que venha da variável de ambiente FRONT_URL.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String frontUrl;

    public CorsConfig(@Value("${FRONT_URL:}") String frontUrl) {
        this.frontUrl = frontUrl;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> origens = new ArrayList<>(List.of("http://localhost:5173", "http://localhost:4173", "https://*.vercel.app"));
        // FRONT_URL pode ter mais de uma URL separada por vírgula; tiramos a barra do fim
        for (String url : frontUrl.split(",")) {
            String limpa = url.trim().replaceAll("/+$", "");
            if (!limpa.isEmpty()) {
                origens.add(limpa);
            }
        }
        registry.addMapping("/**")
                .allowedOriginPatterns(origens.toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type")
                .exposedHeaders("Location");   // o front consegue ler o Location do 201
    }
}
