package com.stage.EnginFlow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

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
        if (statut == null && typeEngin == null && dateCreation == null) {
            return demandeRepository.findAll();
        }

        if (statut != null && !statut.isEmpty() && typeEngin == null && dateCreation == null) {
            return demandeRepository.findByStatutActuel(statut);
        }

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
    @Scheduled(fixedDelay = 3600000) // Toutes les heures
    @Transactional
    public void cloturerDemandes() {
        LocalDateTime maintenant = LocalDateTime.now();
        List<DemandeEngin> demandes = demandeRepository.findDemandesACloturer(maintenant);
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

        // 🔧 CRÉER LA LISTE DES MODIFICATIONS AVEC ANCIENNE ET NOUVELLE VALEUR
        Map<String, Map<String, Object>> modifications = new HashMap<>();

        if (newDateHeureDebut != null && !newDateHeureDebut.equals(demande.getDateHeureDebut())) {
            Map<String, Object> change = new HashMap<>();
            change.put("ancien", demande.getDateHeureDebut().toString());
            change.put("nouveau", newDateHeureDebut.toString());
            modifications.put("dateHeureDebut", change);
        }
        if (newDateHeureFin != null && !newDateHeureFin.equals(demande.getDateHeureFin())) {
            Map<String, Object> change = new HashMap<>();
            change.put("ancien", demande.getDateHeureFin().toString());
            change.put("nouveau", newDateHeureFin.toString());
            modifications.put("dateHeureFin", change);
        }
        if (newLieu != null && !newLieu.equals(demande.getLieu())) {
            Map<String, Object> change = new HashMap<>();
            change.put("ancien", demande.getLieu());
            change.put("nouveau", newLieu);
            modifications.put("lieu", change);
        }
        if (newMotif != null && !newMotif.equals(demande.getMotif())) {
            Map<String, Object> change = new HashMap<>();
            change.put("ancien", demande.getMotif());
            change.put("nouveau", newMotif);
            modifications.put("motif", change);
        }
        if (newCodeInventaire != null && !newCodeInventaire.equals(demande.getEngin().getCodeInventaire())) {
            Map<String, Object> change = new HashMap<>();
            change.put("ancien", demande.getEngin().getCodeInventaire());
            change.put("nouveau", newCodeInventaire);
            modifications.put("codeInventaire", change);
        }

        // Stocker les modifications en JSON
        if (!modifications.isEmpty()) {
            try {
                String modificationsJson = objectMapper.writeValueAsString(modifications);
                demande.setModificationsProposees(modificationsJson);
            } catch (Exception e) {
                throw new RuntimeException("Erreur lors de la sérialisation des modifications");
            }
        }

        // Appliquer les modifications directement
        if (newDateHeureDebut != null) {
            demande.setDateHeureDebut(newDateHeureDebut);
        }
        if (newDateHeureFin != null) {
            demande.setDateHeureFin(newDateHeureFin);
        }
        if (newLieu != null) {
            demande.setLieu(newLieu);
        }
        if (newMotif != null) {
            demande.setMotif(newMotif);
        }
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

        // Appliquer les modifications stockées en JSON
        if (demande.getModificationsProposees() != null) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> modifications = objectMapper.readValue(
                        demande.getModificationsProposees(),
                        Map.class);

                if (modifications.containsKey("dateHeureDebut")) {
                    demande.setDateHeureDebut(LocalDateTime.parse((String) modifications.get("dateHeureDebut")));
                }
                if (modifications.containsKey("dateHeureFin")) {
                    demande.setDateHeureFin(LocalDateTime.parse((String) modifications.get("dateHeureFin")));
                }
                if (modifications.containsKey("lieu")) {
                    demande.setLieu((String) modifications.get("lieu"));
                }
                if (modifications.containsKey("motif")) {
                    demande.setMotif((String) modifications.get("motif"));
                }
                if (modifications.containsKey("codeInventaire")) {
                    String code = (String) modifications.get("codeInventaire");
                    Engin engin = enginRepository.findById(code)
                            .orElseThrow(() -> new RuntimeException("Engin non trouvé"));
                    demande.setEngin(engin);
                }
            } catch (Exception e) {
                throw new RuntimeException("Erreur lors de l'application des modifications");
            }
        }

        demande.setStatutActuel("APPROUVEE");
        demande.setCommentaireAdmin(null);
        demande.setModificationsProposees(null);
        demandeRepository.save(demande);

        DemandeAction action = new DemandeAction();
        action.setTypeAction("VALIDATION");
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

        // ✅ Statut devient REFUSEE (définitif)
        demande.setStatutActuel("REFUSEE");
        demande.setCommentaireAdmin(null);
        demande.setModificationsProposees(null);
        demandeRepository.save(demande);

        DemandeAction action = new DemandeAction();
        action.setTypeAction("REFUS");
        action.setCommentaire("Utilisateur a refusé les modifications");
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