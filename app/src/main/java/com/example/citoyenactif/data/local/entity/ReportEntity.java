package com.example.citoyenactif.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.citoyenactif.model.enums.ReportCategory;
import com.example.citoyenactif.model.enums.ReportStatus;

/**
 * Entité Room représentant la table "reports".
 *
 * Cette classe définit la structure de la table dans la base de données locale (Room) :
 * - colonnes
 * - types
 * - clé primaire
 *
 * Remarque :
 * - Les champs `category` et `status` sont des enums => Room a besoin de TypeConverters
 *   (ex : ReportCategoryConverter / ReportStatusConverter) pour les stocker (souvent en String).
 * - Les @ColumnInfo permettent de définir des noms de colonnes spécifiques (snake_case ici).
 */
@Entity(tableName = "reports")
public class ReportEntity {

    /**
     * Clé primaire auto-générée.
     * Room crée une colonne `id` et génère automatiquement la valeur à l'insertion.
     */
    @PrimaryKey(autoGenerate = true)
    public int id;

    /** Titre du rapport (ex: "Nid-de-poule") */
    public String title;

    /** Description détaillée du problème */
    public String description;

    /**
     * Catégorie du rapport (enum).
     * Exemple : VOIRIE, ECLAIRAGE, PARC, etc.
     * Nécessite un TypeConverter pour être stockée en base.
     */
    public ReportCategory category;

    /**
     * Statut du rapport (enum).
     * Exemple : EN_ATTENTE, EN_COURS, RESOLU, IGNORE.
     * Nécessite un TypeConverter pour être stocké en base.
     */
    public ReportStatus status;

    /**
     * URI (ou chemin) de la photo fournie par le citoyen.
     * @ColumnInfo permet de nommer la colonne "photo_uri" au lieu de "photoUri".
     */
    @ColumnInfo(name = "photo_uri")
    public String photoUri;

    /** Latitude GPS du bris */
    public double latitude;

    /** Longitude GPS du bris */
    public double longitude;

    /**
     * Identifiant du citoyen qui a créé le rapport.
     * Stocké en colonne "citizen_id" (snake_case).
     */
    @ColumnInfo(name = "citizen_id")
    public int citizenId;

    /**
     * Date de création du rapport (timestamp en millisecondes).
     * Stocké en colonne "created_at".
     */
    @ColumnInfo(name = "created_at")
    public long createdAt;

    /**
     * URI de la photo de preuve prise par l’agent quand il met le statut à RESOLU.
     *
     * Remarque :
     * - Si tu veux un nom de colonne snake_case cohérent, tu peux faire :
     *   @ColumnInfo(name = "resolved_photo_uri")
     *   public String resolvedPhotoUri;
     *
     * - Ajouter ce champ à une base existante nécessite une migration Room
     *   (ou uninstall/clear storage en phase de dev).
     */
    public String resolvedPhotoUri; // preuve prise par l’agent (camera)
}