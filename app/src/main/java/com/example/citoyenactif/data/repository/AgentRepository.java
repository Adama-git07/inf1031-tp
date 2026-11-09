package com.example.citoyenactif.data.repository;

import com.example.citoyenactif.CitoyenActifApp;
import com.example.citoyenactif.data.local.dao.AgentDao;
import com.example.citoyenactif.data.local.entity.AgentEntity;

/**
 * Repository des agents municipaux.
 *
 * Fait le lien entre l'UI (ViewModel) et le DAO Room (AgentDao).
 * Ses méthodes font des requêtes Room : à appeler dans un thread secondaire.
 */
public class AgentRepository {

    /** DAO Room pour la table "agents". */
    private final AgentDao agentDao;

    /** Récupère le DAO depuis la base Room de l'application. */
    public AgentRepository() {
        agentDao = CitoyenActifApp.getInstance().getDatabase().agentDao();
    }

    /**
     * Vérifie les identifiants d'un agent (numéro + mot de passe).
     *
     * @return l'agent si les identifiants sont corrects, sinon null
     */
    public AgentEntity login(String agentNumber, String password) {
        return agentDao.login(agentNumber, password);
    }

    /**
     * Cherche un agent par son numéro (sans mot de passe).
     * Utilisé par la connexion par carte NFC.
     *
     * @return l'agent trouvé, sinon null
     */
    public AgentEntity findByNumber(String agentNumber) {
        return agentDao.findByAgentNumber(agentNumber);
    }

    /**
     * Récupère un agent par son id.
     *
     * @return l'agent correspondant, ou null si introuvable
     */
    public AgentEntity getById(long id) {
        return agentDao.getById(id);
    }
}