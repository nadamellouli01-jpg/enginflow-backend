package com.stage.EnginFlow.repository;

import com.stage.EnginFlow.model.Engin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EnginRepository extends JpaRepository<Engin, String> {
    List<Engin> findByTypeEngin(String typeEngin);
}