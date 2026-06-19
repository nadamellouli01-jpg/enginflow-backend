package com.stage.EnginFlow.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PieceJointeRequestDTO {
    private MultipartFile ordreMission;
    private MultipartFile planMaintenance;
}