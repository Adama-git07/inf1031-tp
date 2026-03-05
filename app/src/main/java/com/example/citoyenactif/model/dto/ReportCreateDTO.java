package com.example.citoyenactif.model.dto;

import com.example.citoyenactif.model.enums.ReportCategory;
import com.example.citoyenactif.model.enums.ReportStatus;

/**
 * DTO (Data Transfer Object) utilisé lors de la création d'un rapport de bris.
 *
 * Rôle :
 * - Servir de "conteneur" de données entre la couche UI (formulaire) et la couche Data (Repository/Room)
 * - Éviter d'utiliser directement l'entité Room (ReportEntity) dans l'UI
 *
 * Remarque :
 * - Souvent, le statut n'a pas besoin d'être fourni par l'UI : le repository peut forcer EN_ATTENTE
 *   au moment de l'insertion (ce que tu fais déjà).
 */
public class ReportCreateDTO {

    /** Titre du rapport (ex: "Nid-de-poule important") */
    public String title;

    /** Description détaillée du problème */
    public String description;

    /** Catégorie du bris (VOIRIE, ECLAIRAGE, PARC, etc.) */
    public ReportCategory category;

    /**
     * Statut du rapport.
     * (Optionnel à l'entrée : généralement le repository met EN_ATTENTE automatiquement.)
     */
    public ReportStatus status;

    /** URI de la photo associée au rapport (prise par le citoyen ou choisie) */
    public String photoUri;

    /** Latitude de la position GPS du bris */
    public double latitude;

    /** Longitude de la position GPS du bris */
    public double longitude;

    /** Identifiant du citoyen ayant créé le rapport */
    public int citizenId;
}