package com.stage.EnginFlow.service;

import com.stage.EnginFlow.dto.UtilisateurRequestDTO;
import com.stage.EnginFlow.dto.UtilisateurResponseDTO;
import com.stage.EnginFlow.model.Utilisateur;
import com.stage.EnginFlow.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public UtilisateurResponseDTO creerUtilisateur(UtilisateurRequestDTO request) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(request.getNom());
        utilisateur.setPrenom(request.getPrenom());
        utilisateur.setEmail(request.getEmail());
        utilisateur.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        utilisateur.setRole(request.getRole());

        Utilisateur saved = utilisateurRepository.save(utilisateur);

        return convertToDTO(saved);
    }

    public UtilisateurResponseDTO findByEmail(String email) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return convertToDTO(utilisateur);
    }

    public List<UtilisateurResponseDTO> getAllUtilisateurs() {
        return utilisateurRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private UtilisateurResponseDTO convertToDTO(Utilisateur utilisateur) {
        return UtilisateurResponseDTO.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .email(utilisateur.getEmail())
                .role(utilisateur.getRole())
                .build();
    }
}