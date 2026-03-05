package com.example.citoyenactif.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.citoyenactif.data.local.entity.AgentEntity;

import java.util.List;

/**
 * DAO (Data Access Object) Room pour la table "agents".
 *
 * Rôle :
 * - Définir toutes les opérations SQL autorisées sur les agents :
 *   insertion, lecture, authentification, etc.
 *
 * Remarques :
 * - Les méthodes Room doivent être appelées en thread secondaire (ou via Executor)
 *   pour éviter les accès DB sur le thread UI.
 * - agentNumber est unique (déclaré via @Index(unique=true) dans AgentEntity),
 *   donc on peut l'utiliser comme identifiant métier pour la connexion.
 */
@Dao
public interface AgentDao {

    /**
     * Insère un agent dans la base.
     *
     * onConflict = ABORT :
     * - si un agent avec le même agentNumber existe déjà (index unique),
     *   l'insertion échoue et Room lance une exception.
     *
     * @param agent agent à insérer
     * @return id auto-généré de l'agent inséré
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(AgentEntity agent);

    /**
     * Insère plusieurs agents en une seule opération.
     * Utile pour un seed (données par défaut).
     *
     * onConflict = ABORT :
     * - si au moins un agent est en conflit (agentNumber déjà existant),
     *   l'opération échoue.
     *
     * @param agents liste d'agents à insérer
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insertAll(List<AgentEntity> agents);

    /**
     * Retourne le nombre total d'agents présents dans la table.
     * Utile pour savoir si on doit seed la table ou non.
     */
    @Query("SELECT COUNT(*) FROM agents")
    int countAgent();

    /**
     * Authentifie un agent par son numéro + mot de passe.
     *
     * LIMIT 1 :
     * - retourne au maximum un agent (agentNumber est unique)
     *
     * @param agentNumber numéro d'agent (ex: AGT-1001)
     * @param password mot de passe
     * @return AgentEntity si les identifiants sont corrects, sinon null
     */
    @Query("SELECT * FROM agents WHERE agentNumber = :agentNumber AND password = :password LIMIT 1")
    AgentEntity login(String agentNumber, String password);

    /**
     * Recherche un agent uniquement par agentNumber.
     * Utile pour vérifier l'existence avant insertion (anti-doublon).
     *
     * @param agentNumber numéro d'agent
     * @return AgentEntity si trouvé, sinon null
     */
    @Query("SELECT * FROM agents WHERE agentNumber = :agentNumber LIMIT 1")
    AgentEntity findByAgentNumber(String agentNumber);

    /**
     * Retourne la liste de tous les agents triés par id décroissant (le plus récent en premier).
     * Utile pour des écrans admin ou debug.
     */
    @Query("SELECT * FROM agents ORDER BY id DESC")
    List<AgentEntity> getAll();

    /**
     * Retourne un agent à partir de son id (clé primaire).
     * Utile pour afficher le profil de l'agent connecté.
     *
     * @param id id (clé primaire)
     * @return AgentEntity correspondant, sinon null
     */
    @Query("SELECT * FROM agents WHERE id = :id LIMIT 1")
    AgentEntity getById(long id);
}