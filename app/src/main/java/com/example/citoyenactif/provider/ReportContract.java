package com.example.citoyenactif.provider;

import android.net.Uri;

/**
 * "Contrat" public du ContentProvider des rapports : tout ce dont une AUTRE application
 * a besoin pour interroger Citoyen Actif (authority, URI, noms de colonnes, types MIME).
 *
 * URI disponibles :
 *   content://com.example.citoyenactif.provider/reports                  -> liste (query)
 *   content://com.example.citoyenactif.provider/reports?status=RESOLU    -> liste filtrée (query)
 *   content://com.example.citoyenactif.provider/reports/12               -> un rapport (query)
 *   content://com.example.citoyenactif.provider/reports                  -> création (insert)
 */
public final class ReportContract {

    private ReportContract() { }

    /** Doit être identique à android:authorities du manifeste (${applicationId}.provider). */
    public static final String AUTHORITY = "com.example.citoyenactif.provider";

    public static final String PATH_REPORTS = "reports";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH_REPORTS);

    /** Paramètre d'URI optionnel pour filtrer : EN_ATTENTE, EN_COURS, RESOLU ou IGNORE. */
    public static final String PARAM_STATUS = "status";

    // ── Types MIME ────────────────────────────────────────────────────────────
    public static final String MIME_LIST = "vnd.android.cursor.dir/vnd.com.example.citoyenactif.report";
    public static final String MIME_ITEM = "vnd.android.cursor.item/vnd.com.example.citoyenactif.report";

    // ── Colonnes exposées (citizen_id, photos et agent ne le sont volontairement pas) ──
    public static final String COL_ID          = "_id";
    public static final String COL_TITLE       = "title";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_CATEGORY    = "category";
    public static final String COL_STATUS      = "status";
    public static final String COL_LATITUDE    = "latitude";
    public static final String COL_LONGITUDE   = "longitude";
    public static final String COL_CREATED_AT  = "created_at";

    /** citizenId enregistré pour un rapport créé par une application externe. */
    public static final int EXTERNAL_CITIZEN_ID = 0;
}