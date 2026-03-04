package com.example.citoyenactif.model.enums;

/**
 * Enumération représentant les différents états possibles
 * d'un rapport de bris soumis par un citoyen.
 */
public enum ReportStatus {

    EN_ATTENTE, //Rapport soumis mais pas encore traité par un agent
    EN_COURS, //Rapport pris en charge par un agent municipal
    RESOLU, //Réparation effectuée et confirmée par l'agent
    IGNORE //Rapport ignoré car faux ou doublon
}