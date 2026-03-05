package com.example.citoyenactif.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.example.citoyenactif.data.local.dao.ReportDao;
import com.example.citoyenactif.data.local.database.DatabaseClient;
import com.example.citoyenactif.data.local.entity.ReportEntity;
import com.example.citoyenactif.model.dto.ReportCreateDTO;
import com.example.citoyenactif.model.enums.ReportStatus;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Repository responsable de la gestion des rapports de bris */
public class ReportRepository {

    private final ReportDao reportDao;
    private final ExecutorService executor;

    public ReportRepository(Application application) {
        reportDao = DatabaseClient.getInstance(application).getDb().reportDao();
        executor = Executors.newSingleThreadExecutor();
    }

    /** Crée un nouveau rapport à partir du DTO */
    public void insert(ReportCreateDTO dto) {
        ReportEntity report = new ReportEntity();
        report.title = dto.title;
        report.description = dto.description;
        report.category = dto.category;
        report.status = ReportStatus.EN_ATTENTE;
        report.photoUri = dto.photoUri;
        report.latitude = dto.latitude;
        report.longitude = dto.longitude;
        report.citizenId = dto.citizenId;
        report.createdAt = System.currentTimeMillis();
        executor.execute(() -> reportDao.insert(report));
    }

    public void update(ReportEntity report) {
        executor.execute(() -> reportDao.update(report));
    }

    public void delete(ReportEntity report) {
        executor.execute(() -> reportDao.delete(report));
    }

    public LiveData<List<ReportEntity>> getReportsByCitizen(int citizenId) {
        return reportDao.getReportsByCitizen(citizenId);
    }

    public LiveData<List<ReportEntity>> getAllReports() {
        return reportDao.getAllReports();
    }

    public LiveData<List<ReportEntity>> getPendingReports() {
        return reportDao.getPendingReports();
    }

    public void updateStatus(long reportId, String status) {
        executor.execute(() -> reportDao.updateStatus(reportId, status));
    }

    public void resolveWithProof(long reportId, String proofUri) {
        executor.execute(() ->
                reportDao.resolveWithProof(reportId, ReportStatus.RESOLU.name(), proofUri)
        );
    }
}