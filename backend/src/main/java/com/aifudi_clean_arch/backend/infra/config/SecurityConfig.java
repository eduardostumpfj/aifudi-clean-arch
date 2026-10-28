package com.aifudi_clean_arch.backend.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Default strength/cost factor is 10
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Desabilita o CSRF, já que em APIs REST com tokens/DTOs ele não é necessário
                .csrf(csrf -> csrf.disable())

                // Desabilita aquela tela de formulário do navegador
                .formLogin(form -> form.disable())

                // Desabilita a autenticação básica popup (Http Basic)
                .httpBasic(basic -> basic.disable())

                // Permite que qualquer requisição acesse qualquer rota por enquanto
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}
