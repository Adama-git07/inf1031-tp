package com.example.citoyenactif.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

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
     * Nom du sujet FCM d'un citoyen. Le serveur de notifications publie sur ce sujet
     * (même format dans server/index.js : citizen-<id>).
     */
    public static String citizenTopic(long citizenId) {
        return "citizen-" + citizenId;
    }

    /**
     * Connecte anonymement l'utilisateur à Firebase Authentication.
     * Cela est nécessaire pour que les règles de sécurité Firestore
     * acceptent les opérations de lecture/écriture (require auth != null).
     * Si une session Firebase anonyme existe déjà, aucune nouvelle connexion n'est créée.
     */
    private static void signInFirebaseAnonymously() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            auth.signInAnonymously()
                    .addOnSuccessListener(result ->
                            Log.d("SessionManager", "Connexion Firebase anonyme réussie — uid: "
                                    + result.getUser().getUid()))
                    .addOnFailureListener(e ->
                            Log.e("SessionManager", "Échec connexion Firebase anonyme", e));
        }
    }

    /**
     * Sauvegarde la session d'un citoyen après une connexion réussie.
     * Déclenche aussi une authentification anonyme Firebase pour autoriser
     * les écritures Firestore.
     */
    public static void saveCitizen(Context context, long userId) {
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit()
                .putLong(KEY_USER_ID, userId)
                .putBoolean(KEY_IS_AGENT, false)
                .apply();
        signInFirebaseAnonymously();
    }

    /**
     * Sauvegarde la session d'un agent après authentification.
     * Déclenche aussi une authentification anonyme Firebase pour autoriser
     * les écritures Firestore.
     * Contrairement au citoyen, l'identifiant de l'agent est
     * également sauvegardé afin de pouvoir récupérer ses informations
     * plus tard dans l'application (profil, interventions, etc.).
     */
    public static void saveAgent(Context context, long agentId) {
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit()
                .putLong(KEY_USER_ID, agentId)   // on stocke l'id de l'agent dans la session
                .putBoolean(KEY_IS_AGENT, true)
                .apply();
        signInFirebaseAnonymously();
    }

    /**
     * Fichier séparé de la session : SessionManager.clear() (déconnexion) ne doit pas
     * effacer le token FCM, qui appartient à l'appareil et non à l'utilisateur.
     */
    private static final String PREF_FCM = "citoyen_actif_fcm";
    private static final String KEY_FCM_TOKEN = "fcm_token";

    /** Mémorise le token FCM de cet appareil. */
    public static void saveFcmToken(Context context, String token) {
        context.getSharedPreferences(PREF_FCM, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_FCM_TOKEN, token)
                .apply();
    }

    /** @return le token FCM de cet appareil, ou null s'il n'est pas encore connu. */
    public static String getFcmToken(Context context) {
        return context.getSharedPreferences(PREF_FCM, Context.MODE_PRIVATE)
                .getString(KEY_FCM_TOKEN, null);
    }

    /**
     * Récupère l'identifiant de l'utilisateur actuellement connecté.
     */
    public static long getUserId(Context context) {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getLong(KEY_USER_ID, -1L);
    }

    /**
     * Vérifie si l'utilisateur connecté est un agent.
     */
    public static boolean isAgent(Context context) {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getBoolean(KEY_IS_AGENT, false);
    }

    /**
     * Supprime les informations de session stockées et déconnecte
     * l'utilisateur de Firebase Authentication.
     *
     * Si c'était un citoyen, on le désabonne aussi de son sujet FCM :
     * le téléphone peut être partagé, le prochain utilisateur ne doit pas
     * recevoir les notifications du précédent.
     *
     * @param context contexte de l'application
     */
    public static void clear(Context context) {
        long userId = getUserId(context);
        if (userId > 0 && !isAgent(context)) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(citizenTopic(userId));
        }

        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
        FirebaseAuth.getInstance().signOut();
    }
}