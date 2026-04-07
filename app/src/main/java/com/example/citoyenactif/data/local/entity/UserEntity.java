package com.example.citoyenactif.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.citoyenactif.model.enums.UserRoles;

/**
 * Entité Room représentant la table "users".
 *
 * Version 4 : ajout des champs firstName, lastName, phone, address
 * conformément aux exigences du sujet (nom, prénom, adresse, courriel, téléphone).
 */
@Entity(
        tableName = "users",
        indices = {
                @Index(value = {"username"}, unique = true),
                @Index(value = {"email"}, unique = true)
        }
)
public class UserEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    /** Nom de famille */
    public String lastName;

    /** Prénom */
    public String firstName;

    /** Adresse postale */
    public String address;

    /** Adresse email unique */
    public String email;

    /** Numéro de téléphone */
    public String phone;

    /** Nom d'utilisateur unique */
    public String username;

    /** Mot de passe (stocké en clair — à hasher en phase 2) */
    public String password;

    /** Rôle de l'utilisateur (CITIZEN, AGENT) */
    public UserRoles role;

    /** Timestamp de création du compte (ms) */
    public long createdAt;
}
