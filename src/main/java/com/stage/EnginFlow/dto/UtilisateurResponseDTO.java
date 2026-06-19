package com.stage.EnginFlow.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UtilisateurResponseDTO {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String role;
    // PAS de motDePasse !
}