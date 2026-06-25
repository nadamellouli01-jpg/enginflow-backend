package com.stage.EnginFlow.service;

import com.stage.EnginFlow.dto.UtilisateurRequestDTO;
import com.stage.EnginFlow.dto.UtilisateurResponseDTO;
import com.stage.EnginFlow.model.Utilisateur;
import com.stage.EnginFlow.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

        return UtilisateurResponseDTO.builder()
                .id(saved.getId())
                .nom(saved.getNom())
                .prenom(saved.getPrenom())
                .email(saved.getEmail())
                .role(saved.getRole())
                .build();
    }

    public UtilisateurResponseDTO findByEmail(String email) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return convertToDTO(utilisateur);
    }

    // ✅ NOUVELLE MÉTHODE : Retourne l'entité Utilisateur
    public Utilisateur getUtilisateurByEmail(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
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