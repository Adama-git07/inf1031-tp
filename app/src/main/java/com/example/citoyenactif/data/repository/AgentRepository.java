package com.example.citoyenactif.data.repository;

import com.example.citoyenactif.CitoyenActifApp;
import com.example.citoyenactif.data.local.dao.AgentDao;
import com.example.citoyenactif.data.local.entity.AgentEntity;

/**
 * Repository responsable de la gestion des agents municipaux.
 *
 * Rôle :
 * - Servir d'intermédiaire entre l'UI (Fragments/Activities) et le DAO Room (AgentDao).
 * - Centraliser l'accès aux données liées aux agents.
 *
 * Remarque :
 * - Les méthodes de ce repository (login/getById) font des requêtes Room.
 * - Elles doivent idéalement être appelées dans un thread secondaire
 *   (ou via Executor/LiveData) pour éviter les accès DB sur le thread UI.
 */
public class AgentRepository {

    /**
     * DAO Room pour accéder à la table "agents".
     */
    private final AgentDao agentDao;

    /**
     * Constructeur :
     * - Récupère l'instance unique de la base Room via DatabaseClient
     * - Récupère le DAO AgentDao pour exécuter les opérations SQL
     *
     * @param context contexte Android (on prend généralement applicationContext)
     */
    public AgentRepository() {
        agentDao = CitoyenActifApp.getInstance().getDatabase().agentDao();
    }

    /**
     * Vérifie les identifiants d'un agent (numéro + mot de passe).
     *
     * @param agentNumber numéro d'agent (ex: "AGT-1001")
     * @param password mot de passe de l'agent
     * @return AgentEntity si les identifiants sont corrects, sinon null
     */
    public AgentEntity login(String agentNumber, String password) {
        return agentDao.login(agentNumber, password);
    }

    /**
     * Récupère un agent par son id.
     * Utile pour afficher le profil de l'agent connecté.
     *
     * @param id identifiant de l'agent (clé primaire)
     * @return AgentEntity correspondant, ou null si introuvable
     */
    public AgentEntity getById(long id) {
        return agentDao.getById(id);
    }
}