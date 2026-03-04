package com.example.citoyenactif.model.enums;

/**
 * Enumération représentant les catégories de bris
 * qu'un citoyen peut signaler dans l'application.
 * Minimum 5 catégories requises selon le sujet.
 */
public enum ReportCategory {

    VOIRIE, //Problèmes de routes, trottoirs, nids-de-poule
    ECLAIRAGE, //Lampadaires cassés ou éclairage public défectueux
    PARC, //Équipements de parcs abîmés, bancs cassés
    AQUEDUC, //Fuites d'eau, bouches d'égout, conduites d'eau
    AUTRE //Tout bris ne rentrant pas dans les autres catégories
}