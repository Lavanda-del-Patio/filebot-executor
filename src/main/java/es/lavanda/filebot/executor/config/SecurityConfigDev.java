package es.lavanda.filebot.executor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsUtils;

@EnableWebSecurity
@Configuration
@Profile("dev")
public class SecurityConfigDev {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // http
        //         .authorizeRequests().requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
        //         .antMatchers("/actuator/**")
        //         .permitAll().anyRequest().authenticated();
        http.cors().disable();
        return http.build();
    }
}
