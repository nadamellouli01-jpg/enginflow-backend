package com.stage.EnginFlow.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // PUBLIC : Inscription
                        .requestMatchers("/api/utilisateurs/inscription").permitAll()

                        // PROFIL UTILISATEUR : Tous les utilisateurs connectés
                        .requestMatchers("/api/utilisateurs/me").authenticated()

                        .requestMatchers("/api/demandes").authenticated()

                        // DEMANDEUR + ADMIN : Voir les demandes
                        .requestMatchers("/api/demandes/modifiees").hasAnyRole("DEMANDEUR", "ADMINISTRATEUR")
                        .requestMatchers("/api/demandes/utilisateur/**").authenticated()
                        .requestMatchers("/api/demandes/{id}/utilisateur/**").hasRole("DEMANDEUR")

                        // ADMIN SEULEMENT
                        .requestMatchers("/api/demandes/statistiques").hasRole("ADMINISTRATEUR")
                        .requestMatchers("/api/demandes/{id}/approuver").hasRole("ADMINISTRATEUR")
                        .requestMatchers("/api/demandes/{id}/refuser").hasRole("ADMINISTRATEUR")
                        .requestMatchers("/api/demandes/{id}/admin/**").hasRole("ADMINISTRATEUR")
                        .requestMatchers("/api/utilisateurs/**").hasRole("ADMINISTRATEUR")

                        .anyRequest().authenticated())
                .httpBasic(httpBasic -> {
                });

        return http.build();
    }
}