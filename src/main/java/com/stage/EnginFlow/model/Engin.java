package com.stage.EnginFlow.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "engins")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Engin {

    @Id
    private String codeInventaire;
    private String typeEngin;
    private String capacite;
}