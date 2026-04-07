package com.example.citoyenactif.data.local.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.Executors;

/**
 * DatabaseClient :
 * - Singleton (une seule instance) qui fournit l'accès à la base Room dans toute l'application.
 * - Centralise la création de Room.databaseBuilder(...)
 * - Déclare et enregistre les migrations (version 1 -> 2 -> 3)
 *
 * Avantage :
 * - Évite de créer plusieurs bases (coûteux) et garantit un point d'accès unique (getDb()).
 */
public class DatabaseClient {

    /** Instance unique (Singleton) */
    private static DatabaseClient instance;

    /** Instance RoomDatabase (ta classe @Database : DataBase) */
    private final DataBase db;

    // MIGRATION 1 -> 2 : ajout de la table agents
    /**
     * Migration de la version 1 vers la version 2.
     * Objectif : créer la table "agents" + index unique sur agentNumber.
     *
     * NOTE : Room appelle cette migration automatiquement lors d'un upgrade
     * si l'appli a déjà une BD en version 1 sur l'appareil.
     */
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
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
    /**
     * Migration de la version 2 vers la version 3.
     * Objectif : créer la table "reports" + index utiles (createdByUserId, status, category).
     *
     * - photo_uri, citizen_id, created_at, resolvedPhotoUri...
     * alors qu'ici tu crées :
     * - photoPath, createdByUserId, createdAt...
     *
     * Si tu as déjà une BD sur l'appareil, ce décalage peut provoquer :
     * "Room cannot verify the data integrity..."
     * => Il faut aligner la migration avec la vraie Entity (ou créer une nouvelle migration 3->4).
     */
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

            // Création de la table reports
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `reports` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`title` TEXT, " +
                            "`description` TEXT, " +
                            "`category` TEXT, " +     // enum via TypeConverter (stocké en TEXT)
                            "`status` TEXT, " +       // enum via TypeConverter (stocké en TEXT)
                            "`latitude` REAL NOT NULL, " +
                            "`longitude` REAL NOT NULL, " +
                            "`photoPath` TEXT, " +
                            "`createdByUserId` INTEGER NOT NULL, " +
                            "`createdAt` INTEGER NOT NULL" +
                            ")"
            );

            // Index pour optimiser les requêtes fréquentes (filtre par user, status, category)
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_reports_createdByUserId` ON `reports` (`createdByUserId`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_reports_status` ON `reports` (`status`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_reports_category` ON `reports` (`category`)");
        }
    };

    // MIGRATION 3 -> 4 : ajout des colonnes firstName, lastName, phone, address dans users
    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // SQLite ne supporte pas ALTER TABLE ... ADD MULTIPLE COLUMNS en une commande
            database.execSQL("ALTER TABLE users ADD COLUMN lastName TEXT");
            database.execSQL("ALTER TABLE users ADD COLUMN firstName TEXT");
            database.execSQL("ALTER TABLE users ADD COLUMN address TEXT");
            database.execSQL("ALTER TABLE users ADD COLUMN phone TEXT");
        }
    };

    /**
     * Constructeur privé :
     * - Empêche la création hors Singleton
     * - Construit la base Room
     *
     * @param context contexte application (on utilise getApplicationContext() pour éviter fuites mémoire)
     */
    private DatabaseClient(Context context) {

        db = Room.databaseBuilder(
                        context.getApplicationContext(),
                        DataBase.class,
                        "citoyen_actif_db"
                )
                // Ajout des migrations pour éviter la suppression de la BD lors d'un changement de version
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)

                // Callback appelé uniquement lors de la première création de la BD (fresh install)
                .addCallback(new RoomDatabase.Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase dbSql) {
                        super.onCreate(dbSql);

                        // Exemple : exécuter un "seed" (données par défaut) dans un thread
                        Executors.newSingleThreadExecutor().execute(() -> {
                            // Seed agents si tu veux le garder (insert de comptes par défaut)
                        });
                    }
                })
                .build();
    }

    /**
     * Retourne l'instance unique de DatabaseClient (Singleton).
     * synchronized : thread-safe (évite 2 instances en même temps).
     */
    public static synchronized DatabaseClient getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseClient(context);
        }
        return instance;
    }

    /**
     * Retourne l'instance de la base Room (DataBase).
     * Permet d'accéder aux DAO : userDao(), agentDao(), reportDao(), etc.
     */
    public DataBase getDb() {
        return db;
    }
}