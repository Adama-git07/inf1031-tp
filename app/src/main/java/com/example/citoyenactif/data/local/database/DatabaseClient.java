package com.example.citoyenactif.data.local.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * DatabaseClient :
 * Contient uniquement les migrations Room.
 */
public class DatabaseClient {

    // MIGRATION 1 -> 2 : ajout de la table agents

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

            // Création de la table agents
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `agents` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`name` TEXT, " +
                            "`firstName` TEXT, " +
                            "`agentNumber` TEXT, " +
                            "`password` TEXT" +
                            ")"
            );

            // Index unique pour empêcher 2 agents avec le même agentNumber
            database.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_agents_agentNumber` " +
                            "ON `agents` (`agentNumber`)"
            );
        }
    };

    // MIGRATION 2 -> 3 : ajout de la table reports
    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

            // Création de la table reports
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `reports` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`title` TEXT, " +
                            "`description` TEXT, " +
                            "`category` TEXT, " +
                            "`status` TEXT, " +
                            "`latitude` REAL NOT NULL, " +
                            "`longitude` REAL NOT NULL, " +
                            "`photo_uri` TEXT, " +
                            "`citizen_id` INTEGER NOT NULL, " +
                            "`created_at` INTEGER NOT NULL, " +
                            "`resolvedPhotoUri` TEXT" +
                            ")"
            );

            // Index pour optimiser les requêtes fréquentes (filtre par user, status, category)
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_reports_createdByUserId` ON `reports` (`createdByUserId`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_reports_status` ON `reports` (`status`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_reports_category` ON `reports` (`category`)");
        }
    };

    // MIGRATION 3 -> 4 : ajout des colonnes firstName, lastName, phone, address dans users
    public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // SQLite ne supporte pas ALTER TABLE ... ADD MULTIPLE COLUMNS en une commande
            database.execSQL("ALTER TABLE users ADD COLUMN lastName TEXT");
            database.execSQL("ALTER TABLE users ADD COLUMN firstName TEXT");
            database.execSQL("ALTER TABLE users ADD COLUMN address TEXT");
            database.execSQL("ALTER TABLE users ADD COLUMN phone TEXT");
        }
    };

    // MIGRATION 4 -> 5 : ajout resolved_by_agent_id et ignore_reason dans reports
    public static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE reports ADD COLUMN resolved_by_agent_id INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE reports ADD COLUMN ignore_reason TEXT");
        }
    };

    // ── Instance unique de la base ─────────────────────────────────────────────
    private static volatile DataBase instance;

    /**
     * Retourne l'unique instance Room (créée au premier appel).
     */
    public static DataBase getInstance(Context context) {
        if (instance == null) {
            synchronized (DatabaseClient.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(), DataBase.class, "citoyen_actif_db")
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                            .build();
                }
            }
        }
        return instance;
    }

    private DatabaseClient() {}
}