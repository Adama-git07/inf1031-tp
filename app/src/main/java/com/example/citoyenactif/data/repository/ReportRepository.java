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

/**
 * Repository responsable de la gestion des rapports de bris.
 *
 * Rôle :
 * - Servir de couche intermédiaire entre l'UI (Fragments/Activities) et la base de données Room (DAO).
 * - Encapsuler la logique d'accès aux données (insert/update/delete/queries).
 * - Exécuter les opérations d'écriture (insert/update/delete) dans un thread secondaire.
 *
 * Avantage :
 * - L'UI n'appelle jamais directement le DAO => séparation des responsabilités (architecture propre).
 */
public class ReportRepository {

    /** DAO Room pour accéder à la table "reports" */
    private final ReportDao reportDao;

    /**
     * Executor (thread unique) utilisé pour exécuter les opérations d'écriture
     * (Room interdit les accès DB sur le thread principal).
     */
    private final ExecutorService executor;

    /**
     * Constructeur :
     * - Récupère l'instance unique de la base Room via DatabaseClient
     * - Récupère le ReportDao
     * - Initialise l'executor (un seul thread en file d'attente)
     *
     * @param application contexte Application (plus sûr qu'un contexte Activity)
     */
    public ReportRepository(Application application) {
        reportDao = DatabaseClient.getInstance(application).getDb().reportDao();
        executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Crée un nouveau rapport à partir d'un DTO (données saisies dans l'UI).
     *
     * Étapes :
     * - Convertit le ReportCreateDTO (UI) en ReportEntity (Room)
     * - Force le statut EN_ATTENTE à la création (bonne pratique)
     * - Fixe createdAt avec l'heure actuelle
     * - Insère en base dans un thread secondaire
     *
     * @param dto données du formulaire de création report
     */
    public void insert(ReportCreateDTO dto) {

        // Transformation DTO -> Entity
        ReportEntity report = new ReportEntity();
        report.title = dto.title;
        report.description = dto.description;
        report.category = dto.category;

        // Statut forcé à EN_ATTENTE à la création (même si dto.status existe)
        report.status = ReportStatus.EN_ATTENTE;

        report.photoUri = dto.photoUri;
        report.latitude = dto.latitude;
        report.longitude = dto.longitude;
        report.citizenId = dto.citizenId;

        // Date de création (timestamp en millisecondes)
        report.createdAt = System.currentTimeMillis();

        // Insertion Room en background
        executor.execute(() -> reportDao.insert(report));
    }

    /**
     * Met à jour un rapport existant.
     * Exécuté en arrière-plan.
     */
    public void update(ReportEntity report) {
        executor.execute(() -> reportDao.update(report));
    }

    /**
     * Supprime un rapport.
     * Exécuté en arrière-plan.
     */
    public void delete(ReportEntity report) {
        executor.execute(() -> reportDao.delete(report));
    }

    /**
     * Retourne (en LiveData) la liste des rapports créés par un citoyen.
     * LiveData : l'UI observe et se met à jour automatiquement.
     *
     * @param citizenId id du citoyen
     */
    public LiveData<List<ReportEntity>> getReportsByCitizen(int citizenId) {
        return reportDao.getReportsByCitizen(citizenId);
    }

    /**
     * Retourne (en LiveData) la liste de tous les rapports.
     * Utile pour la vue municipale (agent).
     */
    public LiveData<List<ReportEntity>> getAllReports() {
        return reportDao.getAllReports();
    }

    /**
     * Retourne (en LiveData) seulement les rapports en attente (EN_ATTENTE).
     * Utile pour prioriser les traitements.
     */
    public LiveData<List<ReportEntity>> getPendingReports() {
        return reportDao.getPendingReports();
    }

    /**
     * Met à jour uniquement le statut d'un rapport (ex: EN_COURS, IGNORE, etc.).
     * Exécuté en arrière-plan.
     *
     * @param reportId id du rapport
     * @param status nouveau statut (String, ex: "EN_COURS")
     */
    public void updateStatus(long reportId, String status) {
        executor.execute(() -> reportDao.updateStatus(reportId, status));
    }

    /**
     * Marque un rapport comme RESOLU avec une preuve (URI photo prise par l'agent).
     *
     * - Force le statut à RESOLU (ReportStatus.RESOLU.name())
     * - Enregistre l'URI de preuve dans la colonne resolvedPhotoUri
     * - Exécuté en arrière-plan
     *
     * @param reportId id du rapport
     * @param proofUri URI (String) de la photo de preuve
     */
    public void resolveWithProof(long reportId, String proofUri) {
        executor.execute(() ->
                reportDao.resolveWithProof(reportId, ReportStatus.RESOLU.name(), proofUri)
        );
    }
}