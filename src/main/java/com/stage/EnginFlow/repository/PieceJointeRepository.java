package com.stage.EnginFlow.repository;

import com.stage.EnginFlow.model.PieceJointe;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PieceJointeRepository extends JpaRepository<PieceJointe, Long> {
    List<PieceJointe> findByDemandeId(Long demandeId);
}