package com.stage.EnginFlow.controller;

import com.stage.EnginFlow.dto.UtilisateurRequestDTO;
import com.stage.EnginFlow.dto.UtilisateurResponseDTO;
import com.stage.EnginFlow.service.UtilisateurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/utilisateurs")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Utilisateurs", description = "Gestion des utilisateurs")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    @PostMapping("/inscription")
    @Operation(summary = "Inscription", description = "🔓 Endpoint public - Crée un nouveau compte utilisateur. Pas d'authentification requise.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilisateur créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Email déjà utilisé ou paramètres invalides")
    })
    public UtilisateurResponseDTO inscription(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Informations d'inscription", required = true, content = @Content(schema = @Schema(implementation = UtilisateurRequestDTO.class))) @RequestBody UtilisateurRequestDTO request) {
        return utilisateurService.creerUtilisateur(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Profil utilisateur", description = "Récupère les informations de l'utilisateur connecté")
    public UtilisateurResponseDTO getCurrentUser(Authentication auth) {
        return utilisateurService.findByEmail(auth.getName());
    }
}