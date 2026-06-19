package com.stage.EnginFlow.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;

@Data
public class DemandeRequestDTO {
    private LocalDateTime dateHeureDebut;
    private LocalDateTime dateHeureFin;
    private String lieu;
    private String motif;
    private Long utilisateurId;
    private String codeInventaire;

    private MultipartFile ordreMission;
    private MultipartFile planMaintenance;
}