package com.stage.EnginFlow.controller;

import com.stage.EnginFlow.model.Engin;
import com.stage.EnginFlow.repository.EnginRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/engins")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class EnginController {

    private final EnginRepository enginRepository;

    // Récupérer tous les engins
    @GetMapping
    public List<Engin> getAllEngins() {
        return enginRepository.findAll();
    }

    // Récupérer les engins par type (CAMION, GRUE, PLATEFORME)
    @GetMapping("/type")
    public List<Engin> getEnginsByType(@RequestParam String type) {
        return enginRepository.findByTypeEngin(type);
    }
}