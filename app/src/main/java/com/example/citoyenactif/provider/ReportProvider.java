package com.example.citoyenactif.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Binder;
import android.os.Process;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteQueryBuilder;

import com.example.citoyenactif.data.local.dao.ReportDao;
import com.example.citoyenactif.data.local.database.DatabaseClient;
import com.example.citoyenactif.data.local.entity.ReportEntity;
import com.example.citoyenactif.data.remote.FirestoreRepository;
import com.example.citoyenactif.model.enums.ReportCategory;
import com.example.citoyenactif.model.enums.ReportStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * ContentProvider des signalements.
 *
 * - Les autres applications (ex. Citoyen Social) peuvent seulement LIRE (query).
 * - insert, update et delete sont réservés à Citoyen Actif lui-même (requireOwner).
 *
 * Sécurité :
 *  - permissions de niveau "signature" dans le manifest (lecture et écriture séparées)
 *  - colonnes, tri et sélection validés avec des listes blanches
 *  - les valeurs saisies passent toujours par selectionArgs (jamais collées dans le SQL)
 */
public class ReportProvider extends ContentProvider {

    private static final int REPORTS   = 1;   // .../reports
    private static final int REPORT_ID = 2;   // .../reports/#

    private static final String TABLE = "reports";

    private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    /** Colonnes autorisées : nom exposé -> expression SQL. */
    private static final Map<String, String> COLUMNS = new LinkedHashMap<>();

    /** Tris autorisés : nom exposé -> vraie colonne. */
    private static final Map<String, String> SORT_COLUMNS = new LinkedHashMap<>();

    /** Sélection autorisée : "colonne = ?" ou "colonne LIKE ?", reliés par AND. */
    private static final Pattern SELECTION_PATTERN = Pattern.compile(
            "^(title|category|status)\\s*(=|LIKE)\\s*\\?"
                    + "(\\s+AND\\s+(title|category|status)\\s*(=|LIKE)\\s*\\?)*$",
            Pattern.CASE_INSENSITIVE);

    /** Tri autorisé : "colonne" ou "colonne ASC/DESC". */
    private static final Pattern SORT_PATTERN = Pattern.compile(
            "^(_id|title|category|status|created_at)(\\s+(ASC|DESC))?$",
            Pattern.CASE_INSENSITIVE);

    static {
        MATCHER.addURI(ReportContract.AUTHORITY, ReportContract.PATH_REPORTS, REPORTS);
        MATCHER.addURI(ReportContract.AUTHORITY, ReportContract.PATH_REPORTS + "/#", REPORT_ID);

        COLUMNS.put(ReportContract.COL_ID, "id AS _id");
        COLUMNS.put(ReportContract.COL_TITLE, "title");
        COLUMNS.put(ReportContract.COL_DESCRIPTION, "description");
        COLUMNS.put(ReportContract.COL_CATEGORY, "category");
        COLUMNS.put(ReportContract.COL_STATUS, "status");
        COLUMNS.put(ReportContract.COL_LATITUDE, "latitude");
        COLUMNS.put(ReportContract.COL_LONGITUDE, "longitude");
        COLUMNS.put(ReportContract.COL_CREATED_AT, "created_at");

        SORT_COLUMNS.put("_id", "id");
        SORT_COLUMNS.put("title", "title");
        SORT_COLUMNS.put("category", "category");
        SORT_COLUMNS.put("status", "status");
        SORT_COLUMNS.put("created_at", "created_at");
    }

    @Override
    public boolean onCreate() {
        // La base est créée à la demande par DatabaseClient
        return true;
    }

    // ── query ─────────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        int match = MATCHER.match(uri);
        if (match != REPORTS && match != REPORT_ID) {
            throw new IllegalArgumentException("URI inconnue : " + uri);
        }

        // 1) Colonnes demandées (liste blanche)
        String[] columns = checkProjection(projection);

        // 2) Conditions du WHERE et leurs arguments (même ordre)
        List<String> conditions = new ArrayList<>();
        List<String> args = new ArrayList<>();

        if (selection != null && !selection.trim().isEmpty()) {
            checkSelection(selection.trim(), selectionArgs);
            conditions.add("(" + selection.trim() + ")");
            args.addAll(Arrays.asList(selectionArgs));
        }

        if (match == REPORT_ID) {
            conditions.add("id = ?");
            args.add(String.valueOf(ContentUris.parseId(uri)));
        } else {
            String status = uri.getQueryParameter(ReportContract.PARAM_STATUS);
            if (status != null && !status.trim().isEmpty()) {
                // valueOf lance IllegalArgumentException si le statut n'existe pas
                ReportStatus s = ReportStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
                conditions.add("status = ?");
                args.add(s.name());
            }
        }

        // 3) Tri (liste blanche), par défaut : plus récent d'abord
        String orderBy = checkSortOrder(sortOrder);

        // 4) Construction et exécution de la requête
        String where = conditions.isEmpty() ? null : String.join(" AND ", conditions);
        SupportSQLiteQueryBuilder builder = SupportSQLiteQueryBuilder.builder(TABLE)
                .columns(columns)
                .orderBy(orderBy);
        if (where != null) {
            builder.selection(where, args.toArray());
        }

        Cursor cursor = readableDb().query(builder.create());

        // L'appelant sera prévenu quand on appellera notifyChange sur cette URI
        cursor.setNotificationUri(context().getContentResolver(), uri);
        return cursor;
    }

    // ── Validation ────────────────────────────────────────────────────────────

    /** Retourne les expressions SQL des colonnes demandées (toutes si projection vide). */
    private String[] checkProjection(String[] projection) {
        if (projection == null || projection.length == 0) {
            return COLUMNS.values().toArray(new String[0]);
        }
        String[] result = new String[projection.length];
        for (int i = 0; i < projection.length; i++) {
            String sql = COLUMNS.get(projection[i]);
            if (sql == null) {
                throw new IllegalArgumentException("Colonne non autorisée : " + projection[i]);
            }
            result[i] = sql;
        }
        return result;
    }

    /** Refuse toute sélection qui n'est pas de la forme "colonne = ?" (valeurs dans selectionArgs). */
    private void checkSelection(String selection, String[] selectionArgs) {
        if (!SELECTION_PATTERN.matcher(selection).matches()) {
            throw new IllegalArgumentException("Sélection non autorisée : " + selection);
        }
        int placeholders = 0;
        for (int i = 0; i < selection.length(); i++) {
            if (selection.charAt(i) == '?') placeholders++;
        }
        int given = (selectionArgs == null) ? 0 : selectionArgs.length;
        if (placeholders != given) {
            throw new IllegalArgumentException("Nombre de selectionArgs incorrect");
        }
    }

    /** Retourne un ORDER BY sûr (ex. "created_at DESC"). */
    private String checkSortOrder(String sortOrder) {
        if (sortOrder == null || sortOrder.trim().isEmpty()) {
            return "created_at DESC";
        }
        String clean = sortOrder.trim();
        if (!SORT_PATTERN.matcher(clean).matches()) {
            throw new IllegalArgumentException("Tri non autorisé : " + sortOrder);
        }
        String[] parts = clean.split("\\s+");
        String column = SORT_COLUMNS.get(parts[0].toLowerCase(Locale.ROOT));
        String direction = parts.length > 1 ? parts[1].toUpperCase(Locale.ROOT) : "ASC";
        return column + " " + direction;
    }

    // ── insert / update / delete : réservés à Citoyen Actif (requireOwner) ────

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        requireOwner();
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

        if (title.isEmpty() || title.length() > 100) {
            throw new IllegalArgumentException("title obligatoire (max 100 caractères)");
        }
        if (description.isEmpty() || description.length() > 1000) {
            throw new IllegalArgumentException("description obligatoire (max 1000 caractères)");
        }
        if (latitude == null || longitude == null
                || latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("latitude/longitude invalides");
        }

        // Catégorie : AUTRE par défaut (valueOf refuse une catégorie inconnue)
        String categoryName = clean(values.getAsString(ReportContract.COL_CATEGORY));
        ReportCategory category = categoryName.isEmpty()
                ? ReportCategory.AUTRE
                : ReportCategory.valueOf(categoryName.toUpperCase(Locale.ROOT));

        ReportEntity report = new ReportEntity();
        report.title       = title;
        report.description = description;
        report.category    = category;
        report.status      = ReportStatus.EN_ATTENTE;   // statut toujours forcé
        report.latitude    = latitude;
        report.longitude   = longitude;
        report.citizenId   = ReportContract.EXTERNAL_CITIZEN_ID;
        report.createdAt   = System.currentTimeMillis();

        // Room d'abord (id local), puis Firestore avec le même id
        long id = dao().insert(report);
        report.id = (int) id;
        new FirestoreRepository().pushReport(report);

        context().getContentResolver().notifyChange(ReportContract.CONTENT_URI, null);
        return ContentUris.withAppendedId(ReportContract.CONTENT_URI, id);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        requireOwner();
        if (MATCHER.match(uri) != REPORT_ID) {
            throw new IllegalArgumentException("URI invalide pour update : " + uri);
        }
        if (values == null || values.size() == 0) {
            throw new IllegalArgumentException("Aucune valeur fournie");
        }

        // Seuls ces champs peuvent être modifiés
        ContentValues safe = new ContentValues();
        for (String key : values.keySet()) {
            switch (key) {
                case ReportContract.COL_TITLE:
                case ReportContract.COL_DESCRIPTION: {
                    String text = clean(values.getAsString(key));
                    if (text.isEmpty()) {
                        throw new IllegalArgumentException(key + " vide");
                    }
                    safe.put(key, text);
                    break;
                }
                case ReportContract.COL_STATUS: {
                    // valueOf refuse un statut inconnu
                    ReportStatus s = ReportStatus.valueOf(
                            clean(values.getAsString(key)).toUpperCase(Locale.ROOT));
                    safe.put(key, s.name());
                    break;
                }
                default:
                    throw new IllegalArgumentException("Colonne non modifiable : " + key);
            }
        }

        long id = ContentUris.parseId(uri);
        int rows = writableDb().update(TABLE, SQLiteDatabase.CONFLICT_NONE,
                safe, "id = ?", new Object[]{id});   // id absent => 0 ligne

        if (rows > 0) {
            if (safe.containsKey(ReportContract.COL_STATUS)) {
                new FirestoreRepository().pushStatusUpdate(id,
                        safe.getAsString(ReportContract.COL_STATUS));
            }
            context().getContentResolver().notifyChange(ReportContract.CONTENT_URI, null);
        }
        return rows;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        requireOwner();
        if (MATCHER.match(uri) != REPORT_ID) {
            throw new IllegalArgumentException("URI invalide pour delete : " + uri);
        }

        long id = ContentUris.parseId(uri);
        int rows = writableDb().delete(TABLE, "id = ?", new Object[]{id});   // id absent => 0

        if (rows > 0) {
            new FirestoreRepository().deleteReport((int) id);
            context().getContentResolver().notifyChange(ReportContract.CONTENT_URI, null);
        }
        return rows;
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

    /** Refuse tout appelant qui n'est pas Citoyen Actif lui-même (même uid). */
    private void requireOwner() {
        if (Binder.getCallingUid() != Process.myUid()) {
            throw new SecurityException("Opération réservée à Citoyen Actif");
        }
    }

    private ReportDao dao() {
        return DatabaseClient.getInstance(context()).reportDao();
    }

    private SupportSQLiteDatabase readableDb() {
        return DatabaseClient.getInstance(context()).getOpenHelper().getReadableDatabase();
    }

    private SupportSQLiteDatabase writableDb() {
        return DatabaseClient.getInstance(context()).getOpenHelper().getWritableDatabase();
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