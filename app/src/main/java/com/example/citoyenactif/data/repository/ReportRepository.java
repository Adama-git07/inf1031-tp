package com.example.citoyenactif.data.repository;

import androidx.lifecycle.LiveData;

import com.example.citoyenactif.CitoyenActifApp;
import com.example.citoyenactif.data.local.dao.ReportDao;
import com.example.citoyenactif.data.local.entity.ReportEntity;
import com.example.citoyenactif.model.dto.ReportCreateDTO;
import com.example.citoyenactif.model.enums.ReportStatus;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository responsable de la gestion des rapports de bris.
 *
 * Sert d'intermédiaire entre l'UI et Room.
 * Toutes les opérations d'écriture sont exécutées en arrière-plan via l'executor.
 */
public class ReportRepository {

    private final ReportDao reportDao;
    private final ExecutorService executor;

    public ReportRepository() {
        reportDao = CitoyenActifApp.getInstance().getDatabase().reportDao();
        executor  = Executors.newSingleThreadExecutor();
    }

    /**
     * Insère un nouveau rapport en base.
     *
     * Les champs title/description viennent d'ObservableField (→ .get()).
     * Les photos sont jointes en une chaîne "uri1|uri2|uri3".
     */
    public void insert(ReportCreateDTO dto) {

        ReportEntity report = new ReportEntity();

        report.title       = dto.title.get()       != null ? dto.title.get().trim()       : "";
        report.description = dto.description.get() != null ? dto.description.get().trim() : "";
        report.category    = dto.category;
        report.status      = ReportStatus.EN_ATTENTE;   // statut forcé à la création

        // Plusieurs photos stockées comme "uri1|uri2|..." dans la colonne photo_uri
        if (dto.photoUris != null && !dto.photoUris.isEmpty()) {
            report.photoUri = String.join("|", dto.photoUris);
        } else {
            report.photoUri = null;
        }

        report.latitude   = dto.latitude;
        report.longitude  = dto.longitude;
        report.citizenId  = dto.citizenId;
        report.createdAt  = System.currentTimeMillis();

        executor.execute(() -> reportDao.insert(report));
    }

    /**
     * Met à jour un rapport uniquement si requestingUserId est l'auteur du rapport.
     * Bloque silencieusement toute tentative de modification par un autre utilisateur.
     */
    public void update(ReportEntity report, long requestingUserId) {
        executor.execute(() -> {
            if (report.citizenId != requestingUserId) return;
            if (report.status != ReportStatus.EN_ATTENTE) return;
            reportDao.update(report);
        });
    }

    /** Supprime un rapport. */
    public void delete(ReportEntity report) {
        executor.execute(() -> reportDao.delete(report));
    }

    /** Retourne tous les rapports d'un citoyen (LiveData). */
    public LiveData<List<ReportEntity>> getReportsByCitizen(int citizenId) {
        return reportDao.getReportsByCitizen(citizenId);
    }

    /** Retourne tous les rapports (LiveData) — vue agent. */
    public LiveData<List<ReportEntity>> getAllReports() {
        return reportDao.getAllReports();
    }

    /** Retourne les rapports en attente (LiveData). */
    public LiveData<List<ReportEntity>> getPendingReports() {
        return reportDao.getPendingReports();
    }

    /** Met à jour le statut d'un rapport. */
    public void updateStatus(long reportId, String status) {
        executor.execute(() -> reportDao.updateStatus(reportId, status));
    }

    /** Marque un rapport comme résolu avec preuve photo et ID de l'agent. */
    public void resolveWithProof(long reportId, String proofUri, long agentId) {
        executor.execute(() ->
                reportDao.resolveWithProof(reportId, ReportStatus.RESOLU.name(), proofUri, agentId)
        );
    }

    /** Marque un rapport comme ignoré avec une raison et l'ID de l'agent. */
    public void ignoreWithReason(long reportId, String reason, long agentId) {
        executor.execute(() ->
                reportDao.ignoreWithReason(reportId, ReportStatus.IGNORE.name(), reason, agentId)
        );
    }

    /**
     * Retourne un rapport par son ID (LiveData).
     * Utilisé pour charger un rapport existant dans ReportActivity (mode VIEW/EDIT).
     */
    public LiveData<ReportEntity> getById(int id) {
        return reportDao.getById(id);
    }
}
