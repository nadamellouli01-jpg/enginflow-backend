package com.stage.EnginFlow.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pieces_jointes")
@Data
@NoArgsConstructor
public class PieceJointe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomFichier;
    private String cheminStockage;
    private String typeDocument; // ORDRE_MISSION ou PLAN_MAINTENANCE

    @ManyToOne
    @JoinColumn(name = "demande_id")
    @JsonIgnore
    private DemandeEngin demande;
}