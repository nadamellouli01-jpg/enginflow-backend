package com.stage.EnginFlow.repository;

import com.stage.EnginFlow.model.DemandeEngin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DemandeEnginRepository extends JpaRepository<DemandeEngin, Long> {

        List<DemandeEngin> findByUtilisateurId(Long utilisateurId);

        List<DemandeEngin> findByStatutActuel(String statutActuel);

        List<DemandeEngin> findByUtilisateurIdAndStatutActuel(Long utilisateurId, String statutActuel);

        @Query("SELECT d FROM DemandeEngin d WHERE d.statutActuel = 'APPROUVEE' AND d.dateHeureFin < :dateLimite")
        List<DemandeEngin> findDemandesACloturer(@Param("dateLimite") LocalDateTime dateLimite);

        long countByStatutActuel(String statutActuel);
}