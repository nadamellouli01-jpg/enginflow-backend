package com.stage.EnginFlow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Serveur de développement")))
                .info(new Info()
                        .title("EnginFlow API")
                        .version("1.0")
                        .description("""
                                # API de gestion des demandes d'affectation d'engins

                                ## 🔐 Authentification
                                Utilisez **Basic Auth** avec l'email et le mot de passe.

                                ## 👤 Rôles
                                - **DEMANDEUR** : Créer des demandes, voir ses demandes, confirmer les modifications
                                - **ADMINISTRATEUR** : Tout gérer, dashboard, historique

                                ## 📝 Endpoints réservés
                                - ⚠️ `ADMIN UNIQUEMENT` : Approuver, refuser, modifier, historique, statistiques
                                - ⚠️ `DEMANDEUR UNIQUEMENT` : Demandes modifiées, accepter/refuser modifications
                                """)
                        .contact(new Contact()
                                .name("Stagiaire")
                                .email("stagiaire@email.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .addSecurityItem(new SecurityRequirement().addList("basicAuth"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("basicAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("basic")
                                        .description("""
                                                Authentification Basic Auth

                                                **Exemple :**
                                                - Username: `jean@email.com`
                                                - Password: `123456`

                                                **Admin :**
                                                - Username: `admin@email.com`
                                                - Password: `admin123`
                                                """)));
    }
}