package com.stage.EnginFlow.repository;

import com.stage.EnginFlow.model.DemandeAction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DemandeActionRepository extends JpaRepository<DemandeAction, Long> {
    List<DemandeAction> findByDemandeIdOrderByDateActionDesc(Long demandeId);
}