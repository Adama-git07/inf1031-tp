package com.example.citoyenactif.util;

import com.example.citoyenactif.CitoyenActifApp;
import com.example.citoyenactif.data.local.database.DataBase;
import com.example.citoyenactif.data.local.entity.AgentEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Classe utilitaire pour insérer des agents "manuellement"
 * (ex: comptes municipaux par défaut) sans supprimer la BD.
 */
public class SeedData {

    /**
     * Ajoute une liste d'agents dans la BD si ils n'existent pas déjà.
     * - Ne supprime rien
     * - Évite les doublons grâce à agentNumber (unique)
     */
    public static void seedAgents() {

        Executors.newSingleThreadExecutor().execute(() -> {
            DataBase db = CitoyenActifApp.getInstance().getDatabase();

            // 1) Définis ici tes agents "manuels"
            List<AgentEntity> toInsert = new ArrayList<>();

            AgentEntity a1 = new AgentEntity();
            a1.firstName = "Abiola";
            a1.name = "Nurudeen";
            a1.agentNumber = "01";
            a1.password = "1234";
            toInsert.add(a1);

            AgentEntity a2 = new AgentEntity();
            a2.firstName = "Patrice";
            a2.name = "Dia";
            a2.agentNumber = "02";
            a2.password = "1234";
            toInsert.add(a2);

            // 2) Insertion safe : on check si agentNumber existe déjà
            for (AgentEntity a : toInsert) {
                AgentEntity existing = db.agentDao().findByAgentNumber(a.agentNumber);
                if (existing == null) {
                    db.agentDao().insert(a);
                }
            }
        });
    }

    /**
     * Ajoute UN agent (utile si tu veux appeler ça depuis un bouton admin).
     * Retourne true si inséré, false si déjà existant.
     */
    public static void addOneAgent(String firstName,
                                   String name,
                                   String agentNumber,
                                   String password) {

        Executors.newSingleThreadExecutor().execute(() -> {
            DataBase db = CitoyenActifApp.getInstance().getDatabase();

            AgentEntity existing = db.agentDao().findByAgentNumber(agentNumber);
            if (existing != null) return;

            AgentEntity a = new AgentEntity();
            a.firstName = firstName;
            a.name = name;
            a.agentNumber = agentNumber;
            a.password = password;

            db.agentDao().insert(a);
        });
    }
}
