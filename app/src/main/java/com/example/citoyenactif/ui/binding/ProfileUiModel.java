package com.example.citoyenactif.ui.binding;

/**
 * Textes du profil citoyen, déjà prêts à afficher.
 * Le layout profile_fragment.xml les lit avec @{profile.xxx}.
 */
public class ProfileUiModel {

    /** Prénom + nom (ou username si les deux sont vides) */
    public final String fullName;

    /** Username avec le @ devant */
    public final String username;

    /** "Citoyen" ou "Agent municipal" */
    public final String roleLabel;

    /** Courriel de l'utilisateur */
    public final String email;

    /** Téléphone (ou "—" s'il est vide) */
    public final String phone;

    /** Adresse (ou "—" si elle est vide) */
    public final String address;

    /** Exemple : "Membre depuis le 12 novembre 2026" */
    public final String createdAt;

    public ProfileUiModel(String fullName, String username, String roleLabel,
                          String email, String phone, String address,
                          String createdAt) {
        this.fullName  = fullName;
        this.username  = username;
        this.roleLabel = roleLabel;
        this.email     = email;
        this.phone     = phone;
        this.address   = address;
        this.createdAt = createdAt;
    }
}