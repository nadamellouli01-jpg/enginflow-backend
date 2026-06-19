package com.stage.EnginFlow.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DemandeResponseDTO {
    private Long id;
    private LocalDateTime dateHeureDebut;
    private LocalDateTime dateHeureFin;
    private String lieu;
    private String motif;
    private String statutActuel;
    private LocalDateTime dateCreation;
    private String utilisateurNom;
    private String utilisateurPrenom;
    private String codeInventaire;
    private String typeEngin;
    private List<PieceJointeDTO> piecesJointes;
    private List<DemandeActionDTO> historique;
}