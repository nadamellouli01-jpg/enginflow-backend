package com.stage.EnginFlow.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class DemandeActionDTO {
    private Long id;
    private String typeAction;
    private String commentaire;
    private LocalDateTime dateAction;
    private String utilisateurNom;
}