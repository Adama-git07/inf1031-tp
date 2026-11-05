package com.example.citoyenactif.util;

import com.example.citoyenactif.CitoyenActifApp;
import com.example.citoyenactif.data.local.database.DataBase;
import com.example.citoyenactif.data.local.entity.AgentEntity;
import com.example.citoyenactif.data.local.entity.ReportEntity;
import com.example.citoyenactif.model.enums.ReportCategory;
import com.example.citoyenactif.model.enums.ReportStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Classe utilitaire pour insérer des données par défaut dans la BD
 * sans supprimer les données existantes.
 *
 * Deux types de seed :
 *   - Agents municipaux (comptes par défaut)
 *   - Rapports de bris (données de démonstration autour de Trois-Rivières)
 */
public class SeedData {

    /**
     * Ajoute une liste d'agents dans la BD s'ils n'existent pas déjà.
     * - Ne supprime rien
     * - Évite les doublons grâce à agentNumber (unique)
     */
    public static void seedAgents() {
        Executors.newSingleThreadExecutor().execute(() -> {
            DataBase db = CitoyenActifApp.getInstance().getDatabase();

            // 1) Définis ici tes agents "manuels"
            List<AgentEntity> toInsert = new ArrayList<>();

            AgentEntity a1 = new AgentEntity();
            a1.firstName   = "Abiola";
            a1.name        = "Nurudeen";
            a1.agentNumber = "01";
            a1.password    = "1234";
            toInsert.add(a1);

            AgentEntity a2 = new AgentEntity();
            a2.firstName   = "Patrice";
            a2.name        = "Dia";
            a2.agentNumber = "02";
            a2.password    = "1234";
            toInsert.add(a2);

            // 2) Insertion safe : on vérifie si agentNumber existe déjà
            for (AgentEntity a : toInsert) {
                AgentEntity existing = db.agentDao().findByAgentNumber(a.agentNumber);
                if (existing == null) {
                    db.agentDao().insert(a);
                }
            }
        });
    }

    /**
     * Insère des rapports de démonstration autour de Trois-Rivières.
     * - Ne réinsère pas si des rapports existent déjà (vérification par COUNT)
     * - Coordonnées réelles de rues de Trois-Rivières
     */
    public static void seedReports() {
        Executors.newSingleThreadExecutor().execute(() -> {
            DataBase db = CitoyenActifApp.getInstance().getDatabase();

            // Ne pas réinsérer si des rapports existent déjà
            if (db.reportDao().countReports() > 0) return;

            List<ReportEntity> reports = new ArrayList<>();

            // Nid de poule - Boulevard des Forges
            reports.add(makeReport(
                    "Nid de poule dangereux",
                    "Nid de poule profond sur le boulevard, danger pour les vélos.",
                    ReportCategory.VOIRIE,
                    ReportStatus.EN_ATTENTE,
                    46.3484, -72.5470, 1
            ));

            // Lampadaire brisé - Rue des Ursulines
            reports.add(makeReport(
                    "Lampadaire hors service",
                    "Lampadaire éteint depuis 3 jours, zone mal éclairée la nuit.",
                    ReportCategory.ECLAIRAGE,
                    ReportStatus.EN_COURS,
                    46.3431, -72.5398, 1
            ));

            // Fuite d'eau - Rue Saint-Maurice
            reports.add(makeReport(
                    "Fuite d'eau sur la chaussée",
                    "Eau qui coule en permanence depuis une bouche d'égout.",
                    ReportCategory.AQUEDUC,
                    ReportStatus.EN_ATTENTE,
                    46.3501, -72.5512, 1
            ));

            // Banc brisé - Parc Champlain
            reports.add(makeReport(
                    "Banc de parc brisé",
                    "Banc avec planches cassées, risque de blessure.",
                    ReportCategory.PARC,
                    ReportStatus.EN_ATTENTE,
                    46.3398, -72.5441, 1
            ));

            // Trottoir endommagé - Rue Royale
            reports.add(makeReport(
                    "Trottoir soulevé",
                    "Dalle de béton soulevée, danger pour les piétons.",
                    ReportCategory.VOIRIE,
                    ReportStatus.RESOLU,
                    46.3452, -72.5488, 1
            ));

            // Déchets abandonnés - Rue Laviolette
            reports.add(makeReport(
                    "Déchets abandonnés",
                    "Sacs de déchets laissés sur le trottoir depuis plusieurs jours.",
                    ReportCategory.AUTRE,
                    ReportStatus.EN_ATTENTE,
                    46.3467, -72.5502, 1
            ));

            // Graffiti - Vieux Trois-Rivières
            reports.add(makeReport(
                    "Graffiti sur mur municipal",
                    "Graffiti sur le mur du bâtiment municipal rue des Forges.",
                    ReportCategory.AUTRE,
                    ReportStatus.IGNORE,
                    46.3421, -72.5415, 1
            ));

            // Égout bouché - Rue Hart
            reports.add(makeReport(
                    "Égout bouché",
                    "Égout complètement bouché, eau stagnante sur la chaussée.",
                    ReportCategory.AQUEDUC,
                    ReportStatus.EN_COURS,
                    46.3478, -72.5533, 1
            ));

            // Éclairage parc - Parc Pie-XII
            reports.add(makeReport(
                    "Éclairage parc défectueux",
                    "Trois lampadaires du parc sont hors service.",
                    ReportCategory.ECLAIRAGE,
                    ReportStatus.EN_ATTENTE,
                    46.3412, -72.5460, 1
            ));

            // Glissière endommagée - Boulevard Gene-H-Kruger
            reports.add(makeReport(
                    "Glissière de sécurité endommagée",
                    "Glissière métallique tordue suite à un accident.",
                    ReportCategory.VOIRIE,
                    ReportStatus.EN_ATTENTE,
                    46.3520, -72.5550, 1
            ));

            // 3) Insertion de tous les rapports dans Room
            for (ReportEntity r : reports) {
                db.reportDao().insert(r);
            }

            // Rapports près de l'UQTR — zone piétonne campus
            reports.add(makeReport(
                    "Trottoir glissant",
                    "Trottoir verglacé devant le pavillon Ringuet, dangereux.",
                    ReportCategory.VOIRIE,
                    ReportStatus.EN_ATTENTE,
                    46.3530, -72.5780, 1
            ));

            reports.add(makeReport(
                    "Lampadaire défectueux",
                    "Lampadaire clignotant près du stationnement C.",
                    ReportCategory.ECLAIRAGE,
                    ReportStatus.EN_COURS,
                    46.3525, -72.5775, 1
            ));

            reports.add(makeReport(
                    "Fuite eau fontaine",
                    "Fontaine extérieure qui coule sans arrêt.",
                    ReportCategory.AQUEDUC,
                    ReportStatus.EN_ATTENTE,
                    46.3535, -72.5785, 1
            ));

            reports.add(makeReport(
                    "Banc brisé campus",
                    "Banc près de l'entrée principale cassé.",
                    ReportCategory.PARC,
                    ReportStatus.EN_ATTENTE,
                    46.3528, -72.5770, 1
            ));
        });
    }

    /**
     * Méthode helper pour créer un ReportEntity facilement.
     *
     * @param title       titre du rapport
     * @param description description détaillée
     * @param category    catégorie (VOIRIE, ECLAIRAGE, etc.)
     * @param status      statut initial (EN_ATTENTE, EN_COURS, etc.)
     * @param lat         latitude GPS
     * @param lng         longitude GPS
     * @param citizenId   id du citoyen auteur
     * @return ReportEntity prêt à insérer dans Room
     */
    private static ReportEntity makeReport(String title, String description,
                                           ReportCategory category, ReportStatus status,
                                           double lat, double lng, int citizenId) {
        ReportEntity r  = new ReportEntity();
        r.title         = title;
        r.description   = description;
        r.category      = category;
        r.status        = status;
        r.latitude      = lat;
        r.longitude     = lng;
        r.citizenId     = citizenId;
        r.createdAt     = System.currentTimeMillis();
        return r;
    }

    /**
     * Ajoute UN agent (utile si tu veux appeler ça depuis un bouton admin).
     * Ne fait rien si l'agent existe déjà.
     */
    public static void addOneAgent(String firstName, String name,
                                   String agentNumber, String password) {
        Executors.newSingleThreadExecutor().execute(() -> {
            DataBase db = CitoyenActifApp.getInstance().getDatabase();

            // Vérification doublon
            AgentEntity existing = db.agentDao().findByAgentNumber(agentNumber);
            if (existing != null) return;

            AgentEntity a  = new AgentEntity();
            a.firstName    = firstName;
            a.name         = name;
            a.agentNumber  = agentNumber;
            a.password     = password;

            db.agentDao().insert(a);
        });
    }
}