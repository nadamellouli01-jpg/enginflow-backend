package com.stage.EnginFlow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "demandes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class DemandeEngin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateHeureDebut;
    private LocalDateTime dateHeureFin;
    private String lieu;
    private String motif;
    private String statutActuel; // EN_ATTENTE, MODIFIEE, APPROUVEE, REFUSEE, CLOTUREE

    @CreationTimestamp
    private LocalDateTime dateCreation;
    private Boolean estCloturee = false;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @ManyToOne
    @JoinColumn(name = "engin_code")
    private Engin engin;

    @OneToMany(mappedBy = "demande", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonProperty("pieceJointes")
    private List<PieceJointe> pieceJointes = new ArrayList<>();

    @OneToMany(mappedBy = "demande", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonProperty("historique")
    private List<DemandeAction> actions = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String modificationsProposees;

    private String commentaireAdmin;
}