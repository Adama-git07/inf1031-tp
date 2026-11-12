package com.example.citoyenactif.data.repository;

import androidx.lifecycle.LiveData;

import com.example.citoyenactif.CitoyenActifApp;
import com.example.citoyenactif.data.local.dao.ReportDao;
import com.example.citoyenactif.data.local.entity.ReportEntity;
import com.example.citoyenactif.data.remote.FirestoreRepository;
import com.example.citoyenactif.model.dto.ReportCreateDTO;
import com.example.citoyenactif.model.enums.ReportStatus;
import com.example.citoyenactif.provider.ReportContract;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository des rapports — intermédiaire entre l'UI et les sources de données.
 *
 * Stratégie double source :
 *   - Room    : cache local (lecture réactive via LiveData, fonctionne hors ligne)
 *   - Firestore : cloud (synchronisation après chaque écriture Room)
 *
 * Flux d'écriture : UI → ViewModel → ReportRepository → Room puis → Firestore
 * Flux de lecture : Firestore pull (démarrage) → Room insertOrReplace → LiveData notifie l'UI
 *
 * Après chaque écriture dans Room, on prévient aussi les applications qui lisent
 * les rapports via le ContentProvider (notifyProvider).
 */
public class ReportRepository {

    private final ReportDao           reportDao;
    private final FirestoreRepository firestore;
    private final ExecutorService     executor;

    public ReportRepository() {
        reportDao = CitoyenActifApp.getInstance().getDatabase().reportDao();
        firestore = new FirestoreRepository();
        executor  = Executors.newSingleThreadExecutor();
    }

    /** Prévient les apps externes (ContentObserver) que les rapports ont changé. */
    private void notifyProvider() {
        CitoyenActifApp.getInstance().getContentResolver()
                .notifyChange(ReportContract.CONTENT_URI, null);
    }

    //  ÉCRITURE — Room + Firestore

    /**
     * Insère un rapport dans Room puis le pousse vers Firestore.
     *
     * L'ID auto-généré par Room est récupéré pour être utilisé comme
     * identifiant du document Firestore (garantit la cohérence entre les deux bases).
     */
    public void insert(ReportCreateDTO dto) {

        ReportEntity report = new ReportEntity();

        report.title       = dto.title.get()       != null ? dto.title.get().trim()       : "";
        report.description = dto.description.get() != null ? dto.description.get().trim() : "";
        report.category    = dto.category;
        report.status      = ReportStatus.EN_ATTENTE;

        if (dto.photoUris != null && !dto.photoUris.isEmpty()) {
            report.photoUri = String.join("|", dto.photoUris);
        } else {
            report.photoUri = null;
        }

        report.latitude  = dto.latitude;
        report.longitude = dto.longitude;
        report.citizenId = dto.citizenId;
        report.createdAt = System.currentTimeMillis();

        executor.execute(() -> {
            // 1. Insertion Room → récupération de l'ID généré
            long generatedId = reportDao.insert(report);
            report.id = (int) generatedId;
            notifyProvider();

            // 2. Push Firestore avec le même ID
            firestore.pushReport(report);
        });
    }

    /**
     * Met à jour un rapport si le demandeur en est l'auteur et que le statut
     * est encore EN_ATTENTE. Synchronise ensuite avec Firestore.
     */
    public void update(ReportEntity report, long requestingUserId) {
        executor.execute(() -> {
            if (report.citizenId != requestingUserId) return;
            if (report.status != ReportStatus.EN_ATTENTE) return;
            reportDao.update(report);
            notifyProvider();
            firestore.pushReport(report);
        });
    }

    /**
     * Supprime un rapport de Room et de Firestore.
     */
    public void delete(ReportEntity report) {
        executor.execute(() -> {
            reportDao.delete(report);
            notifyProvider();
            firestore.deleteReport(report.id);
        });
    }

    /**
     * Met à jour le statut d'un rapport dans Room et dans Firestore.
     */
    public void updateStatus(long reportId, String status) {
        executor.execute(() -> {
            reportDao.updateStatus(reportId, status);
            notifyProvider();
            firestore.pushStatusUpdate(reportId, status);
        });
    }

    /**
     * Marque un rapport comme résolu avec preuve photo.
     * Synchronise Room et Firestore.
     */
    public void resolveWithProof(long reportId, String proofUri, long agentId) {
        executor.execute(() -> {
            reportDao.resolveWithProof(reportId, ReportStatus.RESOLU.name(), proofUri, agentId);
            notifyProvider();
            firestore.pushResolveWithProof(reportId, proofUri, agentId);
        });
    }

    /**
     * Marque un rapport comme ignoré avec une raison.
     * Synchronise Room et Firestore.
     */
    public void ignoreWithReason(long reportId, String reason, long agentId) {
        executor.execute(() -> {
            reportDao.ignoreWithReason(reportId, ReportStatus.IGNORE.name(), reason, agentId);
            notifyProvider();
            firestore.pushIgnoreWithReason(reportId, reason, agentId);
        });
    }

    //  SYNCHRONISATION — pull Firestore → Room (appelé au démarrage)

    /**
     * Télécharge tous les rapports depuis Firestore et les upsert dans Room.
     *
     * Utilise insertOrReplace pour ne pas écraser les données plus récentes
     * avec des données obsolètes dans un cas de conflit de version.
     *
     * À appeler une fois au démarrage de l'application (CitoyenActifApp.onCreate).
     */
    public void syncFromFirestore() {
        firestore.pullAllReports(report ->
                executor.execute(() -> {
                    reportDao.insertOrReplace(report);
                    notifyProvider();
                })
        );
    }


    //  LECTURE — LiveData Room

    /** Rapports d'un citoyen (LiveData). */
    public LiveData<List<ReportEntity>> getReportsByCitizen(int citizenId) {
        return reportDao.getReportsByCitizen(citizenId);
    }

    /** Tous les rapports (LiveData) — vue agent. */
    public LiveData<List<ReportEntity>> getAllReports() {
        return reportDao.getAllReports();
    }

    /** Rapports en attente (LiveData). */
    public LiveData<List<ReportEntity>> getPendingReports() {
        return reportDao.getPendingReports();
    }

    /** Rapport par ID (LiveData). */
    public LiveData<ReportEntity> getById(int id) {
        return reportDao.getById(id);
    }
}