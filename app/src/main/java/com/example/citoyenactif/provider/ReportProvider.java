package com.example.citoyenactif.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.citoyenactif.data.local.dao.ReportDao;
import com.example.citoyenactif.data.local.database.DatabaseClient;
import com.example.citoyenactif.data.local.entity.ReportEntity;
import com.example.citoyenactif.data.remote.FirestoreRepository;
import com.example.citoyenactif.model.enums.ReportCategory;
import com.example.citoyenactif.model.enums.ReportStatus;

import java.util.Locale;

/**
 * ContentProvider des signalements : permet à d'autres applications installées sur le
 * téléphone du citoyen de LIRE les rapports (query) et d'en CRÉER (insert).
 */
public class ReportProvider extends ContentProvider {

    private static final int REPORTS   = 1;   // .../reports
    private static final int REPORT_ID = 2;   // .../reports/#

    private static final int MAX_TITLE       = 100;
    private static final int MAX_DESCRIPTION = 1000;

    private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        MATCHER.addURI(ReportContract.AUTHORITY, ReportContract.PATH_REPORTS, REPORTS);
        MATCHER.addURI(ReportContract.AUTHORITY, ReportContract.PATH_REPORTS + "/#", REPORT_ID);
    }

    @Override
    public boolean onCreate() {
        // Rien à initialiser ici (la base est créée à la demande par DatabaseClient)
        return true;
    }

    // ── query ─────────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        Cursor cursor;
        switch (MATCHER.match(uri)) {
            case REPORTS: {
                String status = uri.getQueryParameter(ReportContract.PARAM_STATUS);
                if (status == null || status.trim().isEmpty()) {
                    cursor = dao().getAllCursor();
                } else {
                    cursor = dao().getByStatusCursor(status.trim().toUpperCase(Locale.ROOT));
                }
                break;
            }
            case REPORT_ID:
                cursor = dao().getByIdCursor(ContentUris.parseId(uri));
                break;
            default:
                throw new IllegalArgumentException("URI inconnue : " + uri);
        }

        // L'appelant sera prévenu (ContentObserver) quand on appellera notifyChange sur cette URI
        cursor.setNotificationUri(context().getContentResolver(), uri);
        return cursor;
    }

    // ── insert ────────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (MATCHER.match(uri) != REPORTS) {
            throw new IllegalArgumentException("URI invalide pour insert : " + uri);
        }
        if (values == null) {
            throw new IllegalArgumentException("Aucune valeur fournie");
        }

        String title       = clean(values.getAsString(ReportContract.COL_TITLE));
        String description = clean(values.getAsString(ReportContract.COL_DESCRIPTION));
        Double latitude    = values.getAsDouble(ReportContract.COL_LATITUDE);
        Double longitude   = values.getAsDouble(ReportContract.COL_LONGITUDE);

        if (title.isEmpty() || title.length() > MAX_TITLE) {
            throw new IllegalArgumentException("title obligatoire (max " + MAX_TITLE + " caractères)");
        }
        if (description.isEmpty() || description.length() > MAX_DESCRIPTION) {
            throw new IllegalArgumentException("description obligatoire (max " + MAX_DESCRIPTION + " caractères)");
        }
        if (latitude == null || longitude == null
                || latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("latitude/longitude invalides");
        }

        // Catégorie : AUTRE par défaut si l'appelant n'en donne pas
        String categoryName = clean(values.getAsString(ReportContract.COL_CATEGORY));
        ReportCategory category;
        try {
            category = categoryName.isEmpty()
                    ? ReportCategory.AUTRE
                    : ReportCategory.valueOf(categoryName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Catégorie inconnue : " + categoryName);
        }

        // Le statut est toujours forcé : une app externe ne peut pas "résoudre" un rapport
        ReportEntity report = new ReportEntity();
        report.title       = title;
        report.description = description;
        report.category    = category;
        report.status      = ReportStatus.EN_ATTENTE;
        report.latitude    = latitude;
        report.longitude   = longitude;
        report.citizenId   = ReportContract.EXTERNAL_CITIZEN_ID;
        report.createdAt   = System.currentTimeMillis();

        // Room d'abord (id local), puis Firestore avec le même id — comme ReportRepository.insert
        long id = dao().insert(report);
        report.id = (int) id;
        new FirestoreRepository().pushReport(report);

        context().getContentResolver().notifyChange(ReportContract.CONTENT_URI, null);
        return ContentUris.withAppendedId(ReportContract.CONTENT_URI, id);
    }

    // ── update / delete : non supportés (les agents traitent les rapports dans l'app) ──

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("Modification non autorisée via le ContentProvider");
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("Suppression non autorisée via le ContentProvider");
    }

    // ── getType ───────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (MATCHER.match(uri)) {
            case REPORTS:   return ReportContract.MIME_LIST;
            case REPORT_ID: return ReportContract.MIME_ITEM;
            default:        return null;
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ReportDao dao() {
        return DatabaseClient.getInstance(context()).reportDao();
    }

    private Context context() {
        Context c = getContext();
        if (c == null) throw new IllegalStateException("ContentProvider non attaché");
        return c;
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
