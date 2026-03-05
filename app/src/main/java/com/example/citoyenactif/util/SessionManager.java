package com.example.citoyenactif.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Classe utilitaire permettant de gérer la session utilisateur
 * dans l'application Citoyen Actif.
 *
 * Cette classe utilise SharedPreferences pour stocker localement
 * les informations de l'utilisateur connecté afin de maintenir
 * la session entre les différentes activités ou fragments.
 *
 * Les informations stockées sont :
 * - l'identifiant de l'utilisateur connecté
 * - le type d'utilisateur (citoyen ou agent)
 */
public class SessionManager {

    /**
     * Nom du fichier SharedPreferences utilisé pour stocker
     * les informations de session.
     */
    private static final String PREF = "citoyen_actif_session";

    /**
     * Clé utilisée pour sauvegarder l'identifiant de l'utilisateur.
     */
    private static final String KEY_USER_ID = "user_id";

    /**
     * Clé utilisée pour déterminer si l'utilisateur connecté
     * est un agent ou non.
     */
    private static final String KEY_IS_AGENT = "is_agent";

    /**
     * Sauvegarde la session d'un citoyen après une connexion réussie.
     *
     * @param context contexte de l'application
     * @param userId identifiant du citoyen dans la base de données
     */
    public static void saveCitizen(Context context, long userId) {
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit()
                .putLong(KEY_USER_ID, userId)
                .putBoolean(KEY_IS_AGENT, false)
                .apply();
    }

    /**
     * Sauvegarde la session d'un agent après authentification.
     *
     * Contrairement au citoyen, l'identifiant de l'agent est
     * également sauvegardé afin de pouvoir récupérer ses informations
     * plus tard dans l'application (profil, interventions, etc.).
     *
     * @param context contexte de l'application
     * @param agentId identifiant unique de l'agent dans la base de données
     */
    public static void saveAgent(Context context, long agentId) {
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit()
                .putLong(KEY_USER_ID, agentId)   // on stocke l'id de l'agent dans la session
                .putBoolean(KEY_IS_AGENT, true)
                .apply();
    }

    /**
     * Récupère l'identifiant de l'utilisateur actuellement connecté.
     *
     * @param context contexte de l'application
     * @return l'identifiant utilisateur si présent, sinon -1
     */
    public static long getUserId(Context context) {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getLong(KEY_USER_ID, -1L);
    }

    /**
     * Vérifie si l'utilisateur connecté est un agent.
     *
     * @param context contexte de l'application
     * @return true si l'utilisateur est un agent, false sinon
     */
    public static boolean isAgent(Context context) {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getBoolean(KEY_IS_AGENT, false);
    }

    /**
     * Supprime les informations de session stockées.
     *
     * Cette méthode est généralement utilisée lors de la
     * déconnexion de l'utilisateur afin d'effacer toutes
     * les données de session enregistrées.
     *
     * @param context contexte de l'application
     */
    public static void clear(Context context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }
}