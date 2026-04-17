package com.example.citoyenactif;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.multidex.MultiDexApplication;
import androidx.room.Room;

import android.util.Log;

import com.example.citoyenactif.data.local.database.DataBase;
import com.example.citoyenactif.data.local.database.DatabaseClient;
import com.example.citoyenactif.data.repository.ReportRepository;
import com.example.citoyenactif.ui.BaseActivity;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Classe Application du projet CitoyenActif.
 *
 * Responsabilités :
 *   1. Initialiser la base de données Room avec ses migrations.
 *   2. Implémenter ActivityLifecycleCallbacks pour garder une référence
 *      à la BaseActivity active (utile pour les ViewModels qui en ont besoin).
 *
 * Accessible via {@link #getInstance()} depuis n'importe quelle couche du projet.
 */
public class CitoyenActifApp extends MultiDexApplication
        implements Application.ActivityLifecycleCallbacks {

    // ── Singleton ─────────────────────────────────────────────────────────────
    private static CitoyenActifApp instance;

    // ── Base de données ───────────────────────────────────────────────────────
    private DataBase database;

    /**
     * Référence à l'activité actuellement au premier plan.
     * Mise à jour automatiquement par les callbacks du cycle de vie.
     * Toujours castée en BaseActivity (toutes nos activités en héritent).
     */
    private BaseActivity currentActivity;

    // ── Lifecycle Application ─────────────────────────────────────────────────

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Initialisation de Room avec toutes les migrations
        database = Room.databaseBuilder(this, DataBase.class, "citoyen_actif_db")
                .addMigrations(
                        DatabaseClient.MIGRATION_1_2,
                        DatabaseClient.MIGRATION_2_3,
                        DatabaseClient.MIGRATION_3_4,
                        DatabaseClient.MIGRATION_4_5
                )
                .build();

        // Enregistrement des callbacks du cycle de vie des activités
        registerActivityLifecycleCallbacks(this);

        // Synchronisation initiale : télécharge les rapports Firestore → Room
        // (s'exécute en arrière-plan, ne bloque pas le démarrage)
        new ReportRepository().syncFromFirestore();

        // Récupération explicite du token FCM — visible dans Logcat sous le tag "FCM_TOKEN"
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token ->
                        Log.d("FCM_TOKEN", "Token FCM : " + token))
                .addOnFailureListener(e ->
                        Log.e("FCM_TOKEN", "Impossible d'obtenir le token FCM", e));
    }

    // ── Accesseurs ────────────────────────────────────────────────────────────

    /** Retourne l'instance unique de l'Application (accessible depuis n'importe où). */
    public static CitoyenActifApp getInstance() {
        return instance;
    }

    /** Retourne l'instance unique de la base de données Room. */
    public DataBase getDatabase() {
        return database;
    }

    /**
     * Retourne la BaseActivity actuellement active (au premier plan).
     * Peut être null si aucune activité n'est démarrée.
     *
     * @return la BaseActivity courante, ou null
     */
    @Nullable
    public BaseActivity getCurrentActivity() {
        return currentActivity;
    }

    // ── ActivityLifecycleCallbacks ────────────────────────────────────────────

    @Override
    public void onActivityCreated(@NonNull Activity activity,
                                  @Nullable Bundle savedInstanceState) {
        if (activity instanceof BaseActivity) {
            currentActivity = (BaseActivity) activity;
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        // Met à jour la référence quand une activité revient au premier plan
        if (activity instanceof BaseActivity) {
            currentActivity = (BaseActivity) activity;
        }
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        // Évite de garder une référence à une activité détruite
        if (activity == currentActivity) {
            currentActivity = null;
        }
    }

    // ── Callbacks vides (non utilisés) ────────────────────────────────────────

    @Override public void onActivityStarted(@NonNull Activity activity)  {}
    @Override public void onActivityPaused(@NonNull Activity activity)   {}
    @Override public void onActivityStopped(@NonNull Activity activity)  {}
    @Override public void onActivitySaveInstanceState(@NonNull Activity activity,
                                                      @NonNull Bundle outState) {}
}
