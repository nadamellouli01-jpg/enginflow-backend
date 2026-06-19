package com.stage.EnginFlow.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "demande_actions")
@Data
@NoArgsConstructor
public class DemandeAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String typeAction;
    private String commentaire;

    @CreationTimestamp
    private LocalDateTime dateAction;

    @ManyToOne
    @JoinColumn(name = "demande_id")
    @JsonIgnore
    private DemandeEngin demande;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;
}