package com.stage.EnginFlow.controller;

import com.stage.EnginFlow.dto.UtilisateurResponseDTO;
import com.stage.EnginFlow.model.DemandeAction;
import com.stage.EnginFlow.model.DemandeEngin;
import com.stage.EnginFlow.model.PieceJointe;
import com.stage.EnginFlow.repository.PieceJointeRepository;
import com.stage.EnginFlow.service.DemandeService;
import com.stage.EnginFlow.service.UtilisateurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/demandes")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Demandes", description = "Gestion des demandes d'affectation d'engins")
@SecurityRequirement(name = "basicAuth")
public class DemandeController {

        private final DemandeService demandeService;
        private final UtilisateurService utilisateurService;
        private final PieceJointeRepository pieceJointeRepository;

        // === 1. CREER DEMANDE (DEMANDEUR UNIQUEMENT) ===
        @PostMapping(consumes = "multipart/form-data")
        @Operation(summary = "Créer une nouvelle demande", description = "⚠️ DEMANDEUR UNIQUEMENT - Crée une demande avec 2 fichiers OBLIGATOIRES.\n"
                        +
                        "L'ID de l'utilisateur est récupéré automatiquement via l'authentification.\n" +
                        "Les paramètres sont passés en **multipart/form-data**.\n" +
                        "📎 **Ordre de mission** et **Plan de maintenance** sont OBLIGATOIRES.\n" +
                        "🏷️ **codeInventaire** est le code spécifique de l'engin choisi par l'utilisateur.\n" +
                        "📋 Codes disponibles : CAM-001, CAM-002, CAM-003, GRU-001, PLA-001")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Demande créée avec succès"),
                        @ApiResponse(responseCode = "400", description = "Paramètres invalides ou fichiers manquants"),
                        @ApiResponse(responseCode = "401", description = "Non authentifié"),
                        @ApiResponse(responseCode = "403", description = "Accès refusé - Réservé aux demandeurs")
        })
        public DemandeEngin creerDemande(
                        @Parameter(description = "Date et heure de début (format: yyyy-MM-ddTHH:mm:ss)", required = true, example = "2026-06-25T08:00:00") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateHeureDebut,

                        @Parameter(description = "Date et heure de fin (format: yyyy-MM-ddTHH:mm:ss)", required = true, example = "2026-06-25T17:00:00") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateHeureFin,

                        @Parameter(description = "Lieu d'intervention", required = true, example = "Zone Logistique") @RequestParam String lieu,

                        @Parameter(description = "Motif de la demande", required = true, example = "Transport de marchandises") @RequestParam String motif,

                        @Parameter(description = "Code inventaire de l'engin choisi", required = true, example = "CAM-002") @RequestParam String codeInventaire,

                        @Parameter(description = "Ordre de mission (fichier PDF/TXT) - OBLIGATOIRE", required = true) @RequestParam MultipartFile ordreMission,

                        @Parameter(description = "Plan de maintenance (fichier PDF/TXT) - OBLIGATOIRE", required = true) @RequestParam MultipartFile planMaintenance,

                        Authentication auth)
                        throws IOException {

                // Récupérer l'utilisateur connecté automatiquement
                String email = auth.getName();
                UtilisateurResponseDTO utilisateur = utilisateurService.findByEmail(email);
                Long utilisateurId = utilisateur.getId();

                return demandeService.creerDemande(
                                dateHeureDebut, dateHeureFin, lieu, motif,
                                utilisateurId, codeInventaire,
                                ordreMission, planMaintenance);
        }

        // === 2. LISTE DES DEMANDES AVEC FILTRES ===
        @GetMapping
        @Operation(summary = "Liste des demandes avec filtres", description = "ADMIN : voit toutes les demandes avec filtres (statut, typeEngin, dateCreation)\n"
                        +
                        "DEMANDEUR : voit uniquement ses demandes (les filtres sont ignorés)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès"),
                        @ApiResponse(responseCode = "401", description = "Non authentifié")
        })
        public List<DemandeEngin> getAllDemandes(
                        Authentication auth,
                        @Parameter(description = "Filtrer par statut (EN_ATTENTE, APPROUVEE, REFUSEE, MODIFIEE, CLOTUREE)", required = false) @RequestParam(required = false) String statut,
                        @Parameter(description = "Filtrer par type d'engin (CAMION, GRUE, PLATEFORME)", required = false) @RequestParam(required = false) String typeEngin,
                        @Parameter(description = "Filtrer par date de création (format: yyyy-MM-dd)", required = false) @RequestParam(required = false) String dateCreation) {

                String email = auth.getName();
                UtilisateurResponseDTO utilisateur = utilisateurService.findByEmail(email);

                if ("ADMINISTRATEUR".equals(utilisateur.getRole())) {
                        LocalDateTime date = null;
                        if (dateCreation != null && !dateCreation.isEmpty()) {
                                date = LocalDate.parse(dateCreation).atStartOfDay();
                        }
                        return demandeService.getAllDemandesWithFilters(statut, typeEngin, date);
                } else {
                        return demandeService.getDemandesByUtilisateur(utilisateur.getId());
                }
        }

        // === 3. DETAIL D'UNE DEMANDE ===
        @GetMapping("/{id}")
        @Operation(summary = "Récupérer une demande par ID", description = "ADMIN : voit toutes les demandes\n" +
                        "DEMANDEUR : voit uniquement ses demandes (sans historique ni pièces jointes)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Demande trouvée"),
                        @ApiResponse(responseCode = "403", description = "Accès refusé"),
                        @ApiResponse(responseCode = "404", description = "Demande non trouvée")
        })
        public DemandeEngin getDemandeById(
                        @Parameter(description = "ID de la demande", required = true, example = "1") @PathVariable Long id,
                        Authentication auth) {

                String email = auth.getName();
                UtilisateurResponseDTO utilisateur = utilisateurService.findByEmail(email);

                DemandeEngin demande = demandeService.getDemandeById(id);

                if (!"ADMINISTRATEUR".equals(utilisateur.getRole())) {
                        // Vérifier que la demande appartient au demandeur
                        if (!demande.getUtilisateur().getId().equals(utilisateur.getId())) {
                                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                                "Accès refusé. Vous ne pouvez pas voir les demandes des autres utilisateurs.");
                        }
                        // Cacher l'historique et les pièces jointes
                        demande.setActions(null);
                        demande.setPieceJointes(null);
                }

                return demande;
        }

        // === 4. PIECES JOINTES ===
        @GetMapping("/{id}/pieces")
        @Operation(summary = "Récupérer les pièces jointes", description = "ADMIN : voit toutes les pièces jointes\n" +
                        "DEMANDEUR : voit uniquement les pièces jointes de ses demandes")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Pièces jointes récupérées avec succès"),
                        @ApiResponse(responseCode = "403", description = "Accès refusé")
        })
        public List<PieceJointe> getPiecesByDemande(
                        @Parameter(description = "ID de la demande", required = true, example = "1") @PathVariable Long id,
                        Authentication auth) {

                String email = auth.getName();
                UtilisateurResponseDTO utilisateur = utilisateurService.findByEmail(email);

                DemandeEngin demande = demandeService.getDemandeById(id);

                if (!"ADMINISTRATEUR".equals(utilisateur.getRole())) {
                        if (!demande.getUtilisateur().getId().equals(utilisateur.getId())) {
                                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                                "Accès refusé. Vous ne pouvez pas voir les pièces jointes des autres utilisateurs.");
                        }
                }

                return pieceJointeRepository.findByDemandeId(id);
        }

        // === 5. HISTORIQUE (ADMIN UNIQUEMENT) ===
        @GetMapping("/{id}/historique")
        @Operation(summary = "Historique des actions", description = "⚠️ ADMIN UNIQUEMENT - Récupère l'historique complet d'une demande")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Historique récupéré avec succès"),
                        @ApiResponse(responseCode = "403", description = "Accès refusé - Réservé aux administrateurs")
        })
        public List<DemandeAction> getHistorique(
                        @Parameter(description = "ID de la demande", required = true, example = "1") @PathVariable Long id,
                        Authentication auth) {

                String email = auth.getName();
                UtilisateurResponseDTO utilisateur = utilisateurService.findByEmail(email);

                if (!"ADMINISTRATEUR".equals(utilisateur.getRole())) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                        "Accès refusé. Seul l'administrateur peut voir l'historique.");
                }

                return demandeService.getHistoriqueByDemande(id);
        }

        // === 6. DEMANDES MODIFIEES ===
        @GetMapping("/modifiees")
        @Operation(summary = "Demandes modifiées en attente", description = "DEMANDEUR : voit ses demandes modifiées et peut accepter/refuser\n"
                        +
                        "ADMIN : voit toutes les demandes modifiées et peut approuver/modifier à nouveau/refuser")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Liste des demandes modifiées récupérée avec succès"),
                        @ApiResponse(responseCode = "403", description = "Accès refusé - Authentification requise")
        })
        public List<DemandeEngin> getDemandesModifiees(Authentication auth) {
                String email = auth.getName();
                UtilisateurResponseDTO utilisateur = utilisateurService.findByEmail(email);

                if ("ADMINISTRATEUR".equals(utilisateur.getRole())) {
                        return demandeService.getAllDemandesWithFilters("MODIFIEE", null, null);
                } else {
                        return demandeService.getDemandesModifiees(utilisateur.getId());
                }
        }

        // === 7. STATISTIQUES (ADMIN UNIQUEMENT) ===
        @GetMapping("/statistiques")
        @Operation(summary = "Statistiques dashboard", description = "⚠️ ADMIN UNIQUEMENT - Récupère les statistiques pour le dashboard")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Statistiques récupérées avec succès"),
                        @ApiResponse(responseCode = "403", description = "Accès refusé - Réservé aux administrateurs")
        })
        public Map<String, Long> getStatistiques() {
                return demandeService.getStatistiques();
        }

        // === 8. ADMIN APPROUVE ===
        @PutMapping("/{id}/approuver")
        @Operation(summary = "Approuver une demande", description = "⚠️ ADMIN UNIQUEMENT - Approuve une demande en attente")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Demande approuvée avec succès"),
                        @ApiResponse(responseCode = "403", description = "Accès refusé - Réservé aux administrateurs"),
                        @ApiResponse(responseCode = "404", description = "Demande non trouvée")
        })
        public DemandeEngin approuverDemande(
                        @Parameter(description = "ID de la demande", required = true, example = "1") @PathVariable Long id,
                        @Parameter(description = "Commentaire", required = false, example = "Demande acceptée") @RequestParam(required = false) String commentaire,
                        Authentication auth) {

                // Récupérer l'admin connecté automatiquement
                String email = auth.getName();
                UtilisateurResponseDTO admin = utilisateurService.findByEmail(email);
                Long adminId = admin.getId();

                return demandeService.approuverDemande(id, adminId, commentaire);
        }

        // === 9. ADMIN REFUSE ===
        @PutMapping("/{id}/refuser")
        @Operation(summary = "Refuser une demande", description = "⚠️ ADMIN UNIQUEMENT - Refuse une demande avec un motif")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Demande refusée avec succès"),
                        @ApiResponse(responseCode = "403", description = "Accès refusé - Réservé aux administrateurs"),
                        @ApiResponse(responseCode = "404", description = "Demande non trouvée")
        })
        public DemandeEngin refuserDemande(
                        @Parameter(description = "ID de la demande", required = true, example = "1") @PathVariable Long id,
                        @Parameter(description = "Motif du refus", required = true, example = "Documents manquants") @RequestParam String commentaire,
                        Authentication auth) {

                // Récupérer l'admin connecté automatiquement
                String email = auth.getName();
                UtilisateurResponseDTO admin = utilisateurService.findByEmail(email);
                Long adminId = admin.getId();

                return demandeService.refuserDemande(id, adminId, commentaire);
        }

        // === 10. ADMIN MODIFIE ===
        @PutMapping("/{id}/admin/modifier")
        @Operation(summary = "Modifier une demande", description = "⚠️ ADMIN UNIQUEMENT - Propose des modifications sur une demande")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Demande modifiée avec succès"),
                        @ApiResponse(responseCode = "403", description = "Accès refusé - Réservé aux administrateurs"),
                        @ApiResponse(responseCode = "404", description = "Demande non trouvée")
        })
        public DemandeEngin adminModifierDemande(
                        @Parameter(description = "ID de la demande", required = true, example = "1") @PathVariable Long id,
                        @Parameter(description = "Nouvelle date de début", required = false, example = "2026-06-28T09:00:00") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateHeureDebut,
                        @Parameter(description = "Nouvelle date de fin", required = false, example = "2026-06-28T17:00:00") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateHeureFin,
                        @Parameter(description = "Nouveau lieu", required = false, example = "ZoneB") @RequestParam(required = false) String lieu,
                        @Parameter(description = "Nouveau motif", required = false, example = "Transport modifié") @RequestParam(required = false) String motif,
                        @Parameter(description = "Nouveau code inventaire", required = false, example = "CAM-003") @RequestParam(required = false) String codeInventaire,
                        @Parameter(description = "Commentaire", required = false, example = "Changement de date") @RequestParam(required = false) String commentaire,
                        Authentication auth) {

                // Récupérer l'admin connecté automatiquement
                String email = auth.getName();
                UtilisateurResponseDTO admin = utilisateurService.findByEmail(email);
                Long adminId = admin.getId();

                return demandeService.adminModifierDemande(
                                id, adminId, dateHeureDebut, dateHeureFin,
                                lieu, motif, codeInventaire, commentaire);
        }

        // === 11. UTILISATEUR ACCEPTE MODIFICATION ===
        @PutMapping("/{id}/utilisateur/accepter")
        @Operation(summary = "Accepter les modifications", description = "⚠️ DEMANDEUR UNIQUEMENT - Accepte les modifications proposées par l'admin")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Modification acceptée avec succès"),
                        @ApiResponse(responseCode = "403", description = "Accès refusé - Réservé aux demandeurs"),
                        @ApiResponse(responseCode = "404", description = "Demande non trouvée")
        })
        public DemandeEngin accepterModification(
                        @Parameter(description = "ID de la demande", required = true, example = "1") @PathVariable Long id,
                        Authentication auth) {

                // Récupérer l'utilisateur connecté automatiquement
                String email = auth.getName();
                UtilisateurResponseDTO utilisateur = utilisateurService.findByEmail(email);
                Long utilisateurId = utilisateur.getId();

                return demandeService.accepterModification(id, utilisateurId);
        }

        // === 12. UTILISATEUR REFUSE MODIFICATION ===
        @PutMapping("/{id}/utilisateur/refuser")
        @Operation(summary = "Refuser les modifications", description = "⚠️ DEMANDEUR UNIQUEMENT - Refuse les modifications proposées par l'admin")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Modification refusée avec succès"),
                        @ApiResponse(responseCode = "403", description = "Accès refusé - Réservé aux demandeurs"),
                        @ApiResponse(responseCode = "404", description = "Demande non trouvée")
        })
        public DemandeEngin refuserModification(
                        @Parameter(description = "ID de la demande", required = true, example = "1") @PathVariable Long id,
                        Authentication auth) {

                // Récupérer l'utilisateur connecté automatiquement
                String email = auth.getName();
                UtilisateurResponseDTO utilisateur = utilisateurService.findByEmail(email);
                Long utilisateurId = utilisateur.getId();

                return demandeService.refuserModification(id, utilisateurId);
        }
}