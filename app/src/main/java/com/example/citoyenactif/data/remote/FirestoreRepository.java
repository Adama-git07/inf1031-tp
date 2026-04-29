package com.example.citoyenactif.data.remote;

import android.util.Log;

import com.example.citoyenactif.data.local.entity.ReportEntity;
import com.example.citoyenactif.model.enums.ReportCategory;
import com.example.citoyenactif.model.enums.ReportStatus;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Couche d'accès à Firestore — source de vérité côté serveur.
 *
 * Stratégie de synchronisation :
 *   - Room  = cache local (fonctionne hors ligne)
 *   - Firestore = cloud (partagé entre tous les appareils)
 *
 * Collection Firestore : "reports"
 * Document ID          : String.valueOf(report.id) (même identifiant que Room)
 *
 * Toutes les opérations Firestore sont non-bloquantes (callbacks asynchrones).
 */
public class FirestoreRepository {

    private static final String TAG        = "FirestoreRepo";
    private static final String COLLECTION = "reports";

    // Instance Firestore (singleton Firebase SDK)
    private final FirebaseFirestore db;

    public FirestoreRepository() {
        db = FirebaseFirestore.getInstance();
    }

    //  ÉCRITURE — push depuis Room vers Firestore

    /**
     * Crée ou met à jour un document rapport dans Firestore.
     * Appelé après chaque insertion/modification dans Room.
     *
     * @param report entité Room à synchroniser (doit avoir un id > 0)
     */
    public void pushReport(ReportEntity report) {
        String docId = String.valueOf(report.id);
        db.collection(COLLECTION)
                .document(docId)
                .set(toMap(report))
                .addOnSuccessListener(v ->
                        Log.d(TAG, "Rapport #" + docId + " synchronisé avec Firestore"))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Erreur push rapport #" + docId, e));
    }

    /**
     * Met à jour uniquement le statut d'un rapport dans Firestore.
     *
     * @param reportId id Room du rapport
     * @param status   nouveau statut (ex: "RESOLU")
     */
    public void pushStatusUpdate(long reportId, String status) {
        Map<String, Object> update = new HashMap<>();
        update.put("status", status);

        db.collection(COLLECTION)
                .document(String.valueOf(reportId))
                .update(update)
                .addOnSuccessListener(v ->
                        Log.d(TAG, "Statut rapport #" + reportId + " mis à jour → " + status))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Erreur update statut rapport #" + reportId, e));
    }

    /**
     * Met à jour un rapport résolu (statut + photo preuve + agent) dans Firestore.
     */
    public void pushResolveWithProof(long reportId, String proofUri, long agentId) {
        Map<String, Object> update = new HashMap<>();
        update.put("status",            ReportStatus.RESOLU.name());
        update.put("resolvedPhotoUri",  proofUri);
        update.put("resolvedByAgentId", agentId);

        db.collection(COLLECTION)
                .document(String.valueOf(reportId))
                .update(update)
                .addOnSuccessListener(v ->
                        Log.d(TAG, "Rapport #" + reportId + " marqué RESOLU dans Firestore"))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Erreur resolve rapport #" + reportId, e));
    }

    /**
     * Met à jour un rapport ignoré (statut + raison + agent) dans Firestore.
     */
    public void pushIgnoreWithReason(long reportId, String reason, long agentId) {
        Map<String, Object> update = new HashMap<>();
        update.put("status",            ReportStatus.IGNORE.name());
        update.put("ignoreReason",      reason);
        update.put("resolvedByAgentId", agentId);

        db.collection(COLLECTION)
                .document(String.valueOf(reportId))
                .update(update)
                .addOnSuccessListener(v ->
                        Log.d(TAG, "Rapport #" + reportId + " marqué IGNORE dans Firestore"))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Erreur ignore rapport #" + reportId, e));
    }

    /**
     * Supprime un document rapport de Firestore.
     *
     * @param reportId id Room du rapport à supprimer
     */
    public void deleteReport(long reportId) {
        db.collection(COLLECTION)
                .document(String.valueOf(reportId))
                .delete()
                .addOnSuccessListener(v ->
                        Log.d(TAG, "Rapport #" + reportId + " supprimé de Firestore"))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Erreur suppression rapport #" + reportId, e));
    }

    //  LECTURE — pull depuis Firestore vers Room (synchronisation initiale)


    /**
     * Télécharge tous les rapports depuis Firestore et les insère dans Room
     * via le callback fourni.
     *
     * Appelé au démarrage de l'application pour s'assurer que Room contient
     * les données des autres appareils.
     *
     * @param callback appelé pour chaque rapport Firestore à insérer/mettre à jour dans Room
     */
    public void pullAllReports(OnReportPulledCallback callback) {
        db.collection(COLLECTION)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d(TAG, "Pull Firestore : " + querySnapshot.size() + " rapport(s) reçu(s)");
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        ReportEntity report = fromDocument(doc);
                        if (report != null) {
                            callback.onReportPulled(report);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Erreur pull Firestore", e));
    }

    /**
     * Callback fonctionnel pour traiter chaque rapport reçu de Firestore.
     */
    public interface OnReportPulledCallback {
        void onReportPulled(ReportEntity report);
    }

    //  HELPERS — conversion ReportEntity ↔ Map Firestore

    /**
     * Convertit un ReportEntity en Map pour Firestore.
     * Les enums sont stockés sous forme de String (leur nom).
     */
    private Map<String, Object> toMap(ReportEntity r) {
        Map<String, Object> map = new HashMap<>();
        map.put("id",                r.id);
        map.put("title",             r.title);
        map.put("description",       r.description);
        map.put("category",          r.category != null  ? r.category.name()  : null);
        map.put("status",            r.status   != null  ? r.status.name()    : null);
        map.put("photoUri",          r.photoUri);
        map.put("latitude",          r.latitude);
        map.put("longitude",         r.longitude);
        map.put("citizenId",         r.citizenId);
        map.put("createdAt",         r.createdAt);
        map.put("resolvedPhotoUri",  r.resolvedPhotoUri);
        map.put("resolvedByAgentId", r.resolvedByAgentId);
        map.put("ignoreReason",      r.ignoreReason);
        return map;
    }

    /**
     * Convertit un document Firestore en ReportEntity.
     * Retourne null si le document est invalide.
     */
    private ReportEntity fromDocument(QueryDocumentSnapshot doc) {
        try {
            ReportEntity r = new ReportEntity();

            Long id = doc.getLong("id");
            if (id == null) return null;
            r.id = id.intValue();

            r.title             = doc.getString("title");
            r.description       = doc.getString("description");
            r.photoUri          = doc.getString("photoUri");
            r.resolvedPhotoUri  = doc.getString("resolvedPhotoUri");
            r.ignoreReason      = doc.getString("ignoreReason");

            Double lat = doc.getDouble("latitude");
            Double lng = doc.getDouble("longitude");
            r.latitude  = lat != null ? lat : 0.0;
            r.longitude = lng != null ? lng : 0.0;

            Long citizenId = doc.getLong("citizenId");
            r.citizenId = citizenId != null ? citizenId.intValue() : 0;

            Long createdAt = doc.getLong("createdAt");
            r.createdAt = createdAt != null ? createdAt : 0L;

            Long agentId = doc.getLong("resolvedByAgentId");
            r.resolvedByAgentId = agentId != null ? agentId : 0L;

            // Conversion String → enum (avec protection contre valeurs inconnues)
            String categoryStr = doc.getString("category");
            if (categoryStr != null) {
                try { r.category = ReportCategory.valueOf(categoryStr); }
                catch (IllegalArgumentException ignored) {}
            }

            String statusStr = doc.getString("status");
            if (statusStr != null) {
                try { r.status = ReportStatus.valueOf(statusStr); }
                catch (IllegalArgumentException ignored) { r.status = ReportStatus.EN_ATTENTE; }
            } else {
                r.status = ReportStatus.EN_ATTENTE;
            }

            return r;

        } catch (Exception e) {
            Log.e(TAG, "Erreur conversion document Firestore → ReportEntity", e);
            return null;
        }
    }
}
