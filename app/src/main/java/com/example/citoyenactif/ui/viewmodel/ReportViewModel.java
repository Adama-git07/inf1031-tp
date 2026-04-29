package com.example.citoyenactif.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.citoyenactif.data.local.entity.ReportEntity;
import com.example.citoyenactif.data.repository.ReportRepository;
import com.example.citoyenactif.model.dto.ReportCreateDTO;

import java.util.List;

/**
 * ViewModel partagé pour les rapports de bris.
 *
 * Utilisé par :
 *   - ReportsListFragment (liste citoyen)
 *   - ReportMapFragment (carte des rapports)
 *   - ReportsListMunicipalFragment (liste agent)
 *   - ReportActivity        (création / visualisation / édition)
 *
 * Survit aux rotations d'écran grâce à AndroidViewModel.
 * Chaque méthode délègue au ReportRepository.
 */
public class ReportViewModel extends AndroidViewModel {

    private final ReportRepository repo;
    private LiveData<List<ReportEntity>> allReports;

    public ReportViewModel(@NonNull Application application) {
        super(application);
        repo = new ReportRepository();
    }

    // ── Requêtes en lecture (LiveData) ────────────────────────────────────────

    /** Tous les rapports — vue agent et carte. */
    public LiveData<List<ReportEntity>> getAllReports() {
        if (allReports == null) {
            allReports = repo.getAllReports();
        }
        return allReports;
    }

    /** Rapports d'un citoyen donné — vue citoyen. */
    public LiveData<List<ReportEntity>> getReportsByCitizen(int citizenId) {
        return repo.getReportsByCitizen(citizenId);
    }

    /** Charge un rapport par ID — mode VIEW / EDIT. */
    public LiveData<ReportEntity> getById(int id) {
        return repo.getById(id);
    }

    // ── Opérations d'écriture ─────────────────────────────────────────────────

    public void insert(ReportCreateDTO dto) {
        repo.insert(dto);
    }

    public void delete(ReportEntity report) {
        repo.delete(report);
    }

    public void update(ReportEntity report, long requestingUserId) {
        repo.update(report, requestingUserId);
    }

    /** Met à jour le statut d'un rapport (ex. "EN_ATTENTE" → "EN_COURS"). */
    public void updateStatus(int reportId, String status) {
        repo.updateStatus(reportId, status);
    }

    /** Marque un rapport comme résolu avec preuve photo et ID de l'agent. */
    public void resolveWithProof(long reportId, String proofUri, long agentId) {
        repo.resolveWithProof(reportId, proofUri, agentId);
    }

    /** Marque un rapport comme ignoré avec une raison et l'ID de l'agent. */
    public void ignoreWithReason(long reportId, String reason, long agentId) {
        repo.ignoreWithReason(reportId, reason, agentId);
    }
}
