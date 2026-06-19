package com.stage.EnginFlow.repository;

import com.stage.EnginFlow.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByEmail(String email); // ← Ajoute cette ligne
}