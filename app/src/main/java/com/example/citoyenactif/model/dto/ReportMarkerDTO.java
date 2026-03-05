package com.example.citoyenactif.model.dto;

/**
 * DTO (Data Transfer Object) représentant un marqueur de rapport sur la carte.
 *
 * Objectif :
 * - Transporter uniquement les informations nécessaires à l'affichage sur une carte
 *   (marker Google Maps / Mapbox / etc.)
 * - Éviter d'envoyer/charger toute l'entité ReportEntity complète quand ce n'est pas utile.
 *
 * Contenu typique d'un marker :
 * - id : identifiant du rapport (pour ouvrir le détail si on clique sur le marker)
 * - title : titre affiché dans l'info-bulle
 * - category : catégorie du bris (ex: VOIRIE, ECLAIRAGE...)
 * - status : statut du rapport (ex: EN_ATTENTE, RESOLU...)
 * - latitude/longitude : position GPS du marker
 */
public class ReportMarkerDTO {

    /** Identifiant unique du rapport */
    public int id;

    /** Titre du rapport (affiché sur la carte / info window) */
    public String title;

    /** Catégorie du rapport (souvent sous forme de texte) */
    public String category;

    /** Statut du rapport (souvent sous forme de texte) */
    public String status;

    /** Latitude de la position GPS */
    public double latitude;

    /** Longitude de la position GPS */
    public double longitude;

    /**
     * Constructeur complet pour créer un DTO de marker.
     *
     * @param id identifiant du rapport
     * @param title titre du rapport
     * @param category catégorie du rapport
     * @param status statut du rapport
     * @param latitude latitude GPS
     * @param longitude longitude GPS
     */
    public ReportMarkerDTO(int id, String title, String category, String status,
                           double latitude, double longitude) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}