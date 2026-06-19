package com.stage.EnginFlow.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PieceJointeDTO {
    private Long id;
    private String nomFichier;
    private String url;
    private String typeDocument;
}