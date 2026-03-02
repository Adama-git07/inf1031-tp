package com.example.citoyenactif.model.dto;

/**
 * DTO (Data Transfer Object) utilisé pour l'authentification
 * d'un utilisateur.
 *
 * Cette classe représente les données saisies dans le
 * formulaire de connexion (SignInFrag).
 *
 * Elle permet de transmettre les identifiants au Repository
 * afin de vérifier leur validité dans la base de données.
 */
public class UserLoginDTO {

    /**
     * Identifiant de connexion pouvant être soit
     * le nom d'utilisateur, soit l'adresse courriel.
     */
    public String login;

    /** Mot de passe saisi pour l'authentification */
    public String password;
}
