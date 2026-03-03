package com.example.citoyenactif.data.local.database;

import android.content.Context;

import androidx.room.Room;

/**
 * Classe Singleton permettant de fournir une seule instance
 * de la base de données Room dans toute l'application.
 *
 * Cette classe empêche la création multiple de la base,
 * ce qui pourrait entraîner des problèmes de performance
 * et d'incohérence des données.
 */
public class DatabaseClient {

    /**
     * Instance unique (Singleton) de DatabaseClient.
     */
    private static DatabaseClient instance;

    /**
     * Instance de la base de données Room.
     */
    private final DataBase db;

    /**
     * Constructeur privé.
     *
     * Empêche l'instanciation directe depuis l'extérieur.
     * La base est construite une seule fois.
     *
     * @param context contexte de l'application
     */
    private DatabaseClient(Context context) {

        db = Room.databaseBuilder(
                context.getApplicationContext(),
                DataBase.class,                  // classe de la base
                "citoyen_actif_db"               // nom du fichier SQLite
        ).build();
    }

    /**
     * Fournit l'unique instance de DatabaseClient.
     *
     * synchronized garantit que deux threads ne peuvent pas
     * créer l'instance en même temps.
     *
     * @param context contexte de l'application
     * @return instance unique de DatabaseClient
     */
    public static synchronized DatabaseClient getInstance(Context context) {

        if (instance == null) {
            instance = new DatabaseClient(context);
        }

        return instance;
    }

    /**
     * Permet d'accéder à la base de données Room.
     *
     * @return instance de DataBase
     */
    public DataBase getDb() {
        return db;
    }
}