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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:3001"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // PUBLIC : Inscription
                        .requestMatchers("/api/utilisateurs/inscription").permitAll()

                        // PROFIL UTILISATEUR : Tous les utilisateurs connectés
                        .requestMatchers("/api/utilisateurs/me").authenticated()

                        // ✅ CHANGER MOT DE PASSE : Tous les utilisateurs connectés
                        .requestMatchers("/api/utilisateurs/me/mot-de-passe").authenticated()

                        // DEMANDEUR + ADMIN : Demandes
                        .requestMatchers("/api/demandes").authenticated()
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