package com.example.citoyenactif.work;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

/**
 * Planifie / annule la synchronisation périodique des rapports pour les agents.
 *
 * - schedule() : appelé quand un agent ouvre son espace (MunicipalPageActivity)
 * - cancel()   : appelé à sa déconnexion (les téléphones sont partagés entre agents)
 */
public final class ReportSyncScheduler {

    /** 15 minutes = intervalle minimal autorisé par WorkManager pour un travail périodique. */
    private static final long INTERVAL_MINUTES = 15;

    private ReportSyncScheduler() { }

    /**
     * Planifie la synchronisation toutes les 15 minutes, uniquement avec une connexion réseau.
     *
     * KEEP : si le travail est déjà planifié, on le laisse tel quel
     * (sinon chaque ouverture de l'écran repousserait la prochaine exécution).
     */
    public static void schedule(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                ReportSyncWorker.class, INTERVAL_MINUTES, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context.getApplicationContext())
                .enqueueUniquePeriodicWork(
                        ReportSyncWorker.UNIQUE_NAME,
                        ExistingPeriodicWorkPolicy.KEEP,
                        request);
    }

    /** Arrête la synchronisation périodique. */
    public static void cancel(Context context) {
        WorkManager.getInstance(context.getApplicationContext())
                .cancelUniqueWork(ReportSyncWorker.UNIQUE_NAME);
    }
}
