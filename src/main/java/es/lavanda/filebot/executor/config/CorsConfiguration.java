package es.lavanda.filebot.executor.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class CorsConfiguration implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowCredentials(true)
                .allowedMethods("*")
                .exposedHeaders("*").allowedOrigins("http://localhost:4200", "https://lavandadelpatio.es",
                        "https://pre.lavandadelpatio.es", "https://api.lavandadelpatio.es");
    }
}
