package com.stage.EnginFlow.service;

import com.stage.EnginFlow.model.*;
import com.stage.EnginFlow.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DemandeService {

    private final DemandeEnginRepository demandeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EnginRepository enginRepository;
    private final PieceJointeRepository pieceJointeRepository;
    private final DemandeActionRepository actionRepository;
    private final FileStorageService fileStorageService;

    // === CREER DEMANDE ===
    @Transactional
    public DemandeEngin creerDemande(
            LocalDateTime dateHeureDebut,
            LocalDateTime dateHeureFin,
            String lieu,
            String motif,
            Long utilisateurId,
            String codeInventaire,
            MultipartFile ordreMission,
            MultipartFile planMaintenance) throws IOException {

        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Engin engin = enginRepository.findById(codeInventaire)
                .orElseThrow(() -> new RuntimeException("Engin non trouvé"));

        DemandeEngin demande = new DemandeEngin();
        demande.setDateHeureDebut(dateHeureDebut);
        demande.setDateHeureFin(dateHeureFin);
        demande.setLieu(lieu);
        demande.setMotif(motif);
        demande.setStatutActuel("EN_ATTENTE");
        demande.setUtilisateur(utilisateur);
        demande.setEngin(engin);
        demande.setEstCloturee(false);

        DemandeEngin saved = demandeRepository.save(demande);

        List<PieceJointe> pieces = new ArrayList<>();

        if (ordreMission != null && !ordreMission.isEmpty()) {
            String path = fileStorageService.saveFile(ordreMission);
            PieceJointe piece = new PieceJointe();
            piece.setNomFichier(ordreMission.getOriginalFilename());
            piece.setCheminStockage(path);
            piece.setTypeDocument("ORDRE_MISSION");
            piece.setDemande(saved);
            pieceJointeRepository.save(piece);
            pieces.add(piece);
        }

        if (planMaintenance != null && !planMaintenance.isEmpty()) {
            String path = fileStorageService.saveFile(planMaintenance);
            PieceJointe piece = new PieceJointe();
            piece.setNomFichier(planMaintenance.getOriginalFilename());
            piece.setCheminStockage(path);
            piece.setTypeDocument("PLAN_MAINTENANCE");
            piece.setDemande(saved);
            pieceJointeRepository.save(piece);
            pieces.add(piece);
        }

        DemandeAction action = new DemandeAction();
        action.setTypeAction("SOUMISSION");
        action.setCommentaire("Demande créée avec " + pieces.size() + " fichier(s)");
        action.setDemande(saved);
        action.setUtilisateur(utilisateur);
        actionRepository.save(action);

        return saved;
    }

    // === RECUPERER TOUTES LES DEMANDES (ADMIN) ===
    public List<DemandeEngin> getAllDemandes() {
        return demandeRepository.findAll();
    }

    // === RECUPERER TOUTES LES DEMANDES AVEC FILTRES (ADMIN) ===
    public List<DemandeEngin> getAllDemandesWithFilters(String statut, String typeEngin, LocalDateTime dateCreation) {
        // Si pas de filtres → toutes les demandes
        if (statut == null && typeEngin == null && dateCreation == null) {
            return demandeRepository.findAll();
        }

        // Si filtre par statut uniquement → utiliser la méthode existante
        if (statut != null && !statut.isEmpty() && typeEngin == null && dateCreation == null) {
            return demandeRepository.findByStatutActuel(statut);
        }

        // Pour les autres combinaisons → on filtre en mémoire
        List<DemandeEngin> demandes = demandeRepository.findAll();

        return demandes.stream()
                .filter(d -> statut == null || statut.isEmpty() || d.getStatutActuel().equals(statut))
                .filter(d -> typeEngin == null || typeEngin.isEmpty() || d.getEngin().getTypeEngin().equals(typeEngin))
                .filter(d -> dateCreation == null
                        || d.getDateCreation().toLocalDate().equals(dateCreation.toLocalDate()))
                .collect(Collectors.toList());
    }

    // === RECUPERER DEMANDE PAR ID ===
    public DemandeEngin getDemandeById(Long id) {
        return demandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée avec id: " + id));
    }

    // === RECUPERER DEMANDES D'UN UTILISATEUR ===
    public List<DemandeEngin> getDemandesByUtilisateur(Long utilisateurId) {
        return demandeRepository.findByUtilisateurId(utilisateurId);
    }

    // === RECUPERER DEMANDES PAR STATUT ===
    public List<DemandeEngin> getDemandesByStatut(String statut) {
        return demandeRepository.findByStatutActuel(statut);
    }

    // === RECUPERER DEMANDES MODIFIEES (pour notifications) ===
    public List<DemandeEngin> getDemandesModifiees(Long utilisateurId) {
        return demandeRepository.findByUtilisateurIdAndStatutActuel(utilisateurId, "MODIFIEE");
    }

    // === STATISTIQUES POUR DASHBOARD ADMIN ===
    public Map<String, Long> getStatistiques() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", demandeRepository.count());
        stats.put("enAttente", demandeRepository.countByStatutActuel("EN_ATTENTE"));
        stats.put("approuvees", demandeRepository.countByStatutActuel("APPROUVEE"));
        stats.put("modifiees", demandeRepository.countByStatutActuel("MODIFIEE"));
        stats.put("refusees", demandeRepository.countByStatutActuel("REFUSEE"));
        stats.put("cloturees", demandeRepository.countByStatutActuel("CLOTUREE"));
        return stats;
    }

    // === CLOTURE AUTOMATIQUE APRES 24H ===
    @Scheduled(fixedDelay = 3600000)
    @Transactional
    public void cloturerDemandes() {
        LocalDateTime dateLimite = LocalDateTime.now().minusHours(24);
        List<DemandeEngin> demandes = demandeRepository.findDemandesACloturer(dateLimite);

        for (DemandeEngin demande : demandes) {
            demande.setStatutActuel("CLOTUREE");
            demande.setEstCloturee(true);
            demandeRepository.save(demande);

            DemandeAction action = new DemandeAction();
            action.setTypeAction("CLOTURE");
            action.setCommentaire("Clôture automatique après 24h");
            action.setDemande(demande);
            action.setUtilisateur(demande.getUtilisateur());
            actionRepository.save(action);

            System.out.println("✅ Demande " + demande.getId() + " clôturée automatiquement");
        }
    }

    // === ADMIN APPROUVE ===
    @Transactional
    public DemandeEngin approuverDemande(Long id, Long adminId, String commentaire) {
        DemandeEngin demande = demandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée"));

        Utilisateur admin = utilisateurRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin non trouvé"));

        demande.setStatutActuel("APPROUVEE");
        demandeRepository.save(demande);

        DemandeAction action = new DemandeAction();
        action.setTypeAction("VALIDATION");
        action.setCommentaire(commentaire != null ? commentaire : "Demande approuvée");
        action.setDemande(demande);
        action.setUtilisateur(admin);
        actionRepository.save(action);

        return demande;
    }

    // === ADMIN REFUSE ===
    @Transactional
    public DemandeEngin refuserDemande(Long id, Long adminId, String commentaire) {
        DemandeEngin demande = demandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée"));

        Utilisateur admin = utilisateurRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin non trouvé"));

        demande.setStatutActuel("REFUSEE");
        demandeRepository.save(demande);

        DemandeAction action = new DemandeAction();
        action.setTypeAction("REFUS");
        action.setCommentaire(commentaire != null ? commentaire : "Demande refusée");
        action.setDemande(demande);
        action.setUtilisateur(admin);
        actionRepository.save(action);

        return demande;
    }

    // === ADMIN MODIFIE ===
    @Transactional
    public DemandeEngin adminModifierDemande(
            Long demandeId,
            Long adminId,
            LocalDateTime newDateHeureDebut,
            LocalDateTime newDateHeureFin,
            String newLieu,
            String newMotif,
            String newCodeInventaire,
            String commentaire) {

        DemandeEngin demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée"));

        Utilisateur admin = utilisateurRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin non trouvé"));

        if (!"EN_ATTENTE".equals(demande.getStatutActuel())) {
            throw new RuntimeException("Impossible de modifier une demande déjà traitée");
        }

        if (newDateHeureDebut != null)
            demande.setDateHeureDebut(newDateHeureDebut);
        if (newDateHeureFin != null)
            demande.setDateHeureFin(newDateHeureFin);
        if (newLieu != null)
            demande.setLieu(newLieu);
        if (newMotif != null)
            demande.setMotif(newMotif);
        if (newCodeInventaire != null) {
            Engin newEngin = enginRepository.findById(newCodeInventaire)
                    .orElseThrow(() -> new RuntimeException("Engin non trouvé"));
            demande.setEngin(newEngin);
        }

        demande.setCommentaireAdmin(commentaire);
        demande.setStatutActuel("MODIFIEE");
        demandeRepository.save(demande);

        DemandeAction action = new DemandeAction();
        action.setTypeAction("MODIFICATION");
        action.setCommentaire("Admin a modifié la demande : " + (commentaire != null ? commentaire : ""));
        action.setDemande(demande);
        action.setUtilisateur(admin);
        actionRepository.save(action);

        return demande;
    }

    // === UTILISATEUR ACCEPTE MODIFICATION ===
    @Transactional
    public DemandeEngin accepterModification(Long demandeId, Long utilisateurId) {
        DemandeEngin demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée"));

        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!demande.getUtilisateur().getId().equals(utilisateurId)) {
            throw new RuntimeException("Vous n'êtes pas autorisé");
        }

        if (!"MODIFIEE".equals(demande.getStatutActuel())) {
            throw new RuntimeException("Cette demande n'est pas en attente de confirmation");
        }

        demande.setStatutActuel("APPROUVEE");
        demande.setCommentaireAdmin(null);
        demande.setModificationsProposees(null);
        demandeRepository.save(demande);

        DemandeAction action = new DemandeAction();
        action.setTypeAction("CLOTURE");
        action.setCommentaire("Utilisateur a accepté les modifications");
        action.setDemande(demande);
        action.setUtilisateur(utilisateur);
        actionRepository.save(action);

        return demande;
    }

    // === UTILISATEUR REFUSE MODIFICATION ===
    @Transactional
    public DemandeEngin refuserModification(Long demandeId, Long utilisateurId) {
        DemandeEngin demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée"));

        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!demande.getUtilisateur().getId().equals(utilisateurId)) {
            throw new RuntimeException("Vous n'êtes pas autorisé");
        }

        if (!"MODIFIEE".equals(demande.getStatutActuel())) {
            throw new RuntimeException("Cette demande n'est pas en attente de confirmation");
        }

        demande.setStatutActuel("EN_ATTENTE");
        demande.setCommentaireAdmin(null);
        demande.setModificationsProposees(null);
        demandeRepository.save(demande);

        DemandeAction action = new DemandeAction();
        action.setTypeAction("REFUS");
        action.setCommentaire("Utilisateur a refusé les modifications. Admin peut re-décider.");
        action.setDemande(demande);
        action.setUtilisateur(utilisateur);
        actionRepository.save(action);

        return demande;
    }

    // === HISTORIQUE ===
    public List<DemandeAction> getHistoriqueByDemande(Long demandeId) {
        return actionRepository.findByDemandeIdOrderByDateActionDesc(demandeId);
    }
}