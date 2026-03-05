package com.example.citoyenactif.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entité Room représentant la table "agents".
 *
 * Cette table stocke les comptes des agents municipaux qui peuvent :
 * - se connecter via leur numéro d'agent + mot de passe
 * - traiter les rapports (changer le statut, ajouter une preuve, etc.)
 *
 * Points importants :
 * - @Entity : indique à Room que cette classe correspond à une table SQL.
 * - tableName = "agents" : nom exact de la table dans la BD.
 * - @Index(unique = true) sur agentNumber : empêche d'avoir deux agents avec le même numéro.
 */
@Entity(
        tableName = "agents",
        indices = {@Index(value = {"agentNumber"}, unique = true)}
)
public class AgentEntity {

    /**
     * Clé primaire auto-générée.
     * Room créera une colonne "id" et attribuera automatiquement une valeur unique à chaque agent.
     */
    @PrimaryKey(autoGenerate = true)
    public long id;

    /**
     * Nom de famille de l'agent (ex: "Dia", "Nurudeen").
     */
    public String name;

    /**
     * Prénom de l'agent (ex: "Patrice", "Abiola").
     */
    public String firstName;

    /**
     * Numéro d'agent unique (ex: "AGT-1001").
     * Grâce à l'index unique, Room/SQLite empêchera les doublons.
     */
    public String agentNumber;

    /**
     * Mot de passe de l'agent.
     * ⚠️ En production, on évite de stocker un mot de passe en clair.
     * Idéalement : stocker un hash (BCrypt/Argon2) + salt.
     */
    public String password;
}