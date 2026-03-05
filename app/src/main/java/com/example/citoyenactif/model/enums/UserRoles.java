package com.example.citoyenactif.model.enums;

/**
 * Enumération des rôles possibles d'un utilisateur dans l'application.
 *
 * Un enum permet de définir un ensemble fermé (liste fixe) de valeurs.
 * Ici, un utilisateur peut être soit :
 * - CITIZEN  : un citoyen qui crée des rapports
 * - EMPLOYEE : un employé/agent municipal qui traite les rapports
 *
 * Avantages :
 * - évite les erreurs de saisie (ex: "citizen", "Citizen", "CITZEN"...)
 * - facilite les tests (if/switch sur des valeurs connues)
 * - s'intègre bien avec Room via un TypeConverter (stockage en String)
 */
public enum UserRoles {

    /** Rôle citoyen : peut créer et consulter ses rapports */
    CITIZEN,

    /** Rôle employé/agent : peut gérer/traiter les rapports */
    EMPLOYEE
}