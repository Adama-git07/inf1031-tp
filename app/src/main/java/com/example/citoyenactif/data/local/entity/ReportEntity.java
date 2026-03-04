package com.example.citoyenactif.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.citoyenactif.model.enums.ReportCategory;
import com.example.citoyenactif.model.enums.ReportStatus;

/**
 * Entité Room représentant un rapport de bris urbain.
 *
 * Un rapport est créé par un citoyen et contient :
 * - un titre et une description
 * - une catégorie
 * - un statut (en attente, en cours, résolu, ignoré)
 * - une localisation (latitude/longitude)
 * - une photo (optionnelle)
 */
@Entity(
        tableName = "reports",
        indices = {
                @Index(value = {"createdByUserId"}),
                @Index(value = {"status"}),
                @Index(value = {"category"})
        }
)
public class ReportEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String title;
    public String description;

    /** Catégorie du bris (VOIRIE, ECLAIRAGE, PARC, AQUEDUC, AUTRE) */
    public ReportCategory category;

    /** Statut du rapport (EN_ATTENTE, EN_COURS, RESOLU, IGNORE) */
    public ReportStatus status;

    /** Position GPS */
    public double latitude;
    public double longitude;

    /** Chemin local ou URI de la photo (optionnel) */
    public String photoPath;

    /** Identifiant du citoyen ayant créé le rapport */
    public long createdByUserId;

    /** Date de création (timestamp en ms) */
    public long createdAt;
}