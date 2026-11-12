package com.example.citoyenactif.work;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.citoyenactif.data.local.dao.ReportDao;
import com.example.citoyenactif.data.local.database.DatabaseClient;
import com.example.citoyenactif.data.local.entity.ReportEntity;
import com.example.citoyenactif.data.remote.FirestoreRepository;
import com.example.citoyenactif.provider.ReportContract;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Tâche d'arrière-plan : met à jour la liste des bris (Room) des agents municipaux
 * à partir de Firestore. Planifiée toutes les 15 minutes par {@link ReportSyncScheduler}.
 *
 * doWork() s'exécute déjà sur un thread de fond (géré par WorkManager) :
 * on peut donc appeler Firestore en mode bloquant et écrire dans Room directement.
 * Comme l'écran des agents observe Room avec un LiveData, la liste se rafraîchit toute seule.
 */
public class ReportSyncWorker extends Worker {

    private static final String TAG = "ReportSyncWorker";

    /** Nom unique du travail périodique (évite d'en planifier plusieurs). */
    public static final String UNIQUE_NAME = "report_sync_periodic";

    public ReportSyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            // Pas de CitoyenActifApp.getInstance() ici : le process peut avoir été
            // relancé par WorkManager. DatabaseClient fournit l'instance Room partagée.
            ReportDao dao = DatabaseClient.getInstance(getApplicationContext()).reportDao();

            List<ReportEntity> remoteReports = new FirestoreRepository().pullAllReportsBlocking();
            for (ReportEntity report : remoteReports) {
                dao.insertOrReplace(report);
            }

            // Prévient les apps externes (ContentProvider) que la liste a changé
            getApplicationContext().getContentResolver()
                    .notifyChange(ReportContract.CONTENT_URI, null);

            Log.d(TAG, "Synchronisation terminée : " + remoteReports.size() + " rapport(s)");
            return Result.success();

        } catch (ExecutionException e) {
            // Réseau coupé, règles Firestore, utilisateur non authentifié... : on réessaiera
            Log.e(TAG, "Échec de la synchronisation, nouvelle tentative plus tard", e);
            return Result.retry();
        } catch (InterruptedException e) {
            // Le travail a été arrêté par le système : on respecte l'interruption
            Thread.currentThread().interrupt();
            return Result.retry();
        }
    }
}