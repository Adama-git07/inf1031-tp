package com.example.citoyenactif.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.citoyenactif.data.local.entity.ReportEntity;

import java.util.List;

/**
 * DAO (Data Access Object) Room pour la table "reports".
 *
 * Rôle :
 * - Définir toutes les opérations SQL autorisées sur les rapports :
 *   insertion, mise à jour, suppression, et requêtes de lecture.
 *
 * Remarques importantes :
 * - Les méthodes qui retournent LiveData permettent une mise à jour automatique de l'UI :
 *   dès qu'une ligne change dans la table, l'observer (fragment/activité) est notifié.
 * - Les colonnes utilisées dans les requêtes (citizen_id, created_at) doivent correspondre
 *   exactement aux @ColumnInfo de ReportEntity.
 */
@Dao
public interface ReportDao {

    /**
     * Insère un nouveau rapport dans la base.
     * @param report entité ReportEntity à insérer
     */
    @Insert
    void insert(ReportEntity report);

    /**
     * Met à jour toutes les colonnes d'un rapport existant.
     * @param report rapport avec un id existant
     */
    @Update
    void update(ReportEntity report);

    /**
     * Supprime un rapport de la base.
     * @param report rapport à supprimer
     */
    @Delete
    void delete(ReportEntity report);

    /**
     * Retourne (en LiveData) la liste des rapports d'un citoyen (filtrés par citizen_id),
     * triés par date de création décroissante.
     *
     * - citizen_id correspond à @ColumnInfo(name="citizen_id") dans ReportEntity.
     * - created_at correspond à @ColumnInfo(name="created_at") dans ReportEntity.
     *
     * @param citizenId id du citoyen
     * @return LiveData liste de rapports (mise à jour automatiquement)
     */
    @Query("SELECT * FROM reports WHERE citizen_id = :citizenId ORDER BY created_at DESC")
    LiveData<List<ReportEntity>> getReportsByCitizen(int citizenId);

    /**
     * Retourne (en LiveData) tous les rapports, triés du plus récent au plus ancien.
     * Utile surtout pour la vue municipale (agents).
     *
     * @return LiveData liste complète des rapports
     */
    @Query("SELECT * FROM reports ORDER BY created_at DESC")
    LiveData<List<ReportEntity>> getAllReports();

    /**
     * Retourne (en LiveData) uniquement les rapports en attente.
     *
     * Ici tu filtres directement par la chaîne 'EN_ATTENTE'.
     * Cela suppose que ReportStatus est stocké en String (via TypeConverter),
     * et que la valeur en DB est exactement "EN_ATTENTE".
     *
     * @return LiveData liste des rapports en attente
     */
    @Query("SELECT * FROM reports WHERE status = 'EN_ATTENTE' ORDER BY created_at DESC")
    LiveData<List<ReportEntity>> getPendingReports();

    /**
     * Met à jour uniquement le statut d'un rapport.
     * - ReportStatus est ici passé comme String (ex: ReportStatus.EN_COURS.name()).
     *
     * @param reportId id du rapport
     * @param status nouveau statut sous forme de String
     */
    @Query("UPDATE reports SET status = :status WHERE id = :reportId")
    void updateStatus(long reportId, String status);

    /**
     * Cas spécial : marquer un rapport comme résolu avec preuve photo (URI).
     *
     * - Met à jour en une seule requête :
     *   status + resolvedPhotoUri (preuve prise par l'agent)
     *
     * @param reportId id du rapport
     * @param status statut (normalement "RESOLU")
     * @param proofUri URI de la photo de preuve
     */
    @Query("UPDATE reports SET status = :status, resolvedPhotoUri = :proofUri WHERE id = :reportId")
    void resolveWithProof(long reportId, String status, String proofUri);
}