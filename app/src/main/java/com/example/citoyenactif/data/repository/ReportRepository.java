package com.example.citoyenactif.data.repository;

import android.content.Context;

import com.example.citoyenactif.data.local.dao.ReportDao;
import com.example.citoyenactif.data.local.database.DatabaseClient;
import com.example.citoyenactif.data.local.entity.ReportEntity;
import com.example.citoyenactif.model.enums.ReportStatus;

import java.util.List;

/**
 * Repository responsable de la gestion des rapports.
 *
 * Il agit comme une couche intermédiaire entre :
 * - UI (Activities/Fragments)
 * - DAO (Room)
 *
 * L'objectif est d'isoler la logique d'accès aux données.
 */
public class ReportRepository {

    private final ReportDao reportDao;

    /**
     * Initialise le repository en récupérant le DAO via DatabaseClient.
     *
     * @param context contexte application
     */
    public ReportRepository(Context context) {
        reportDao = DatabaseClient.getInstance(context).getDb().reportDao();
    }

    /**
     * Ajoute un rapport dans la base.
     *
     * @param report rapport à insérer
     * @return id auto-généré du rapport inséré
     */
    public long addReport(ReportEntity report) {
        return reportDao.insert(report);
    }

    /**
     * Met à jour un rapport existant.
     */
    public int updateReport(ReportEntity report) {
        return reportDao.update(report);
    }

    /**
     * Supprime un rapport.
     */
    public int deleteReport(ReportEntity report) {
        return reportDao.delete(report);
    }

    /**
     * Retourne tous les rapports.
     */
    public List<ReportEntity> getAllReports() {
        return reportDao.getAll();
    }

    /**
     * Retourne les rapports d'un citoyen.
     *
     * @param userId id du citoyen
     */
    public List<ReportEntity> getReportsByUser(long userId) {
        return reportDao.getByUser(userId);
    }

    /**
     * Retourne les rapports selon un statut.
     *
     * @param status statut recherché
     */
    public List<ReportEntity> getReportsByStatus(ReportStatus status) {
        return reportDao.getByStatus(status);
    }

    /**
     * Retourne un rapport par id.
     */
    public ReportEntity getReportById(long reportId) {
        return reportDao.getById(reportId);
    }

    /**
     * Met à jour uniquement le statut d'un rapport.
     */
    public int updateReportStatus(long reportId, ReportStatus status) {
        return reportDao.updateStatus(reportId, status);
    }
}