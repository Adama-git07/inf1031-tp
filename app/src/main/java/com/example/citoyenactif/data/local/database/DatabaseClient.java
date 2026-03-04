package com.example.citoyenactif.data.local.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.Executors;

public class DatabaseClient {

    private static DatabaseClient instance;
    private final DataBase db;

    // ===== MIGRATION 1 -> 2 : ajout table agents =====
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `agents` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`name` TEXT, " +
                            "`firstName` TEXT, " +
                            "`agentNumber` TEXT, " +
                            "`password` TEXT" +
                            ")"
            );

            database.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_agents_agentNumber` " +
                            "ON `agents` (`agentNumber`)"
            );
        }
    };

    // ===== MIGRATION 2 -> 3 : ajout table reports =====
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `reports` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`title` TEXT, " +
                            "`description` TEXT, " +
                            "`category` TEXT, " +     // enum via TypeConverter
                            "`status` TEXT, " +       // enum via TypeConverter
                            "`latitude` REAL NOT NULL, " +
                            "`longitude` REAL NOT NULL, " +
                            "`photoPath` TEXT, " +
                            "`createdByUserId` INTEGER NOT NULL, " +
                            "`createdAt` INTEGER NOT NULL" +
                            ")"
            );

            database.execSQL("CREATE INDEX IF NOT EXISTS `index_reports_createdByUserId` ON `reports` (`createdByUserId`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_reports_status` ON `reports` (`status`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_reports_category` ON `reports` (`category`)");
        }
    };

    private DatabaseClient(Context context) {

        db = Room.databaseBuilder(
                        context.getApplicationContext(),
                        DataBase.class,
                        "citoyen_actif_db"
                )
                // ✅ IMPORTANT : on ajoute les migrations
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)

                // (Optionnel) ton callback seed agents peut rester
                .addCallback(new RoomDatabase.Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase dbSql) {
                        super.onCreate(dbSql);

                        Executors.newSingleThreadExecutor().execute(() -> {
                            // ton seed agents si tu veux le garder
                        });
                    }
                })
                .build();
    }

    public static synchronized DatabaseClient getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseClient(context);
        }
        return instance;
    }

    public DataBase getDb() {
        return db;
    }
}