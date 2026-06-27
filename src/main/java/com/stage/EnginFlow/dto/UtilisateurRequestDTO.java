package com.stage.EnginFlow.dto;

import lombok.Data;

@Data
public class UtilisateurRequestDTO {
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;

}