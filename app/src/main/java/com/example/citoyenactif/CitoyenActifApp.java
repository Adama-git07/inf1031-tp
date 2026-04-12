package com.example.citoyenactif;

import androidx.multidex.MultiDexApplication;
import androidx.room.Room;

import com.example.citoyenactif.data.local.database.DataBase;
import com.example.citoyenactif.data.local.database.DatabaseClient;

public class CitoyenActifApp extends MultiDexApplication {

    private static CitoyenActifApp instance;
    private DataBase database;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        database = Room.databaseBuilder(this, DataBase.class, "citoyen_actif_db")
                .addMigrations(
                        DatabaseClient.MIGRATION_1_2,
                        DatabaseClient.MIGRATION_2_3,
                        DatabaseClient.MIGRATION_3_4
                )
                .build();
    }

    /** Retourne l'instance unique de l'application (accessible depuis n'importe où). */
    public static CitoyenActifApp getInstance() {
        return instance;
    }

    /** Retourne l'instance unique de la base de données Room. */
    public DataBase getDatabase() {
        return database;
    }
}