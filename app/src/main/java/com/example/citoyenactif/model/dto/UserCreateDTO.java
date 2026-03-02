package com.example.citoyenactif.model.dto;

import com.example.citoyenactif.model.enums.UserRoles;

/**
 * DTO (Data Transfer Object) utilisé lors de la création
 * d’un nouvel utilisateur.
 *
 * Cette classe représente les données saisies par l'utilisateur
 * dans le formulaire d'inscription (SignUpFrag).
 *
 * Elle permet de transporter les informations depuis la couche UI
 * vers la couche Repository sans exposer directement l'entité Room.
 *
 * Les données contenues ici seront converties en UserEntity
 * avant d'être enregistrées dans la base de données locale.
 */
public class UserCreateDTO {

    /** Nom d'utilisateur unique */
    public String username;

    /** Mot de passe saisi lors de la création du compte */
    public String password;

    /** Adresse courriel unique de l'utilisateur */
    public String email;

    /** Rôle de l'utilisateur */
    public UserRoles roles;
}
