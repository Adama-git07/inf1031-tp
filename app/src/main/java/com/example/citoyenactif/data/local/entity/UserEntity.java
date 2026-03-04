package com.example.citoyenactif.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.citoyenactif.model.enums.UserRoles;

/**
 * Entité Room représentant la table "users"
 * dans la base de données locale.
 *
 * Cette classe définit la structure des colonnes
 * et les contraintes associées.
 */
@Entity(
        tableName = "users",

        // Déclaration d'index uniques pour éviter les doublons
        indices = {
                @Index(value = {"username"}, unique = true),
                @Index(value = {"email"}, unique = true)
        }
)
public class UserEntity {

    /**
     * Clé primaire auto-générée.
     * Chaque utilisateur possède un identifiant unique.
     */
    @PrimaryKey(autoGenerate = true)
    public long id;

    /**
     * Nom d'utilisateur unique.
     * Contrainte d'unicité définie via @Index.
     */
    public String username;

    /**
     * Adresse email unique.
     * Contrainte d'unicité définie via @Index.
     */
    public String email;

    /**
     * Mot de passe de l'utilisateur
     */
    public String password; //si possible on va utiliser un HashMap pour eviter de stocker le mot de passe en clair

    /**
     * Rôle de l'utilisateur dans l'application.
     * Enum (CITIZEN, EMPLOYEE, etc.).
     *
     * Utilise un TypeConverter pour être stocké
     * sous forme de String dans la base Room.
     */
    public UserRoles role;

    /**
     * Date de création du compte.
     * Stockée sous forme de timestamp (millisecondes).
     */
    public long createdAt;

}
