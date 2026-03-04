package com.example.citoyenactif.data.local.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.citoyenactif.data.local.entity.AgentEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Classe Singleton permettant de fournir une seule instance
 * de la base de données Room dans toute l'application.
 */
public class DatabaseClient {

    private static DatabaseClient instance;
    private final DataBase db;

    private DatabaseClient(Context context) {

        db = Room.databaseBuilder(
                        context.getApplicationContext(),
                        DataBase.class,
                        "citoyen_actif_db"
                )
                //évite les erreurs de migration pendant le développement
                .fallbackToDestructiveMigration()

                //Seed : création des comptes agents par défaut à la création de la BD
                .addCallback(new RoomDatabase.Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase dbSql) {
                        super.onCreate(dbSql);

                        Executors.newSingleThreadExecutor().execute(() -> {
                            DataBase dbInstance = DatabaseClient.getInstance(context).getDb();

                            // si aucun agent n'existe encore, on en insère par défaut
                            if (dbInstance.agentDao().countAgent() == 0) {

                                AgentEntity a1 = new AgentEntity();
                                a1.firstName = "Abiola";
                                a1.name = "Nurudeen";
                                a1.agentNumber = "AGT-1001";
                                a1.password = "1234";

                                AgentEntity a2 = new AgentEntity();
                                a2.firstName = "Patrice";
                                a2.name = "Dia";
                                a2.agentNumber = "AGT-1002";
                                a2.password = "1234";

                                List<AgentEntity> list = new ArrayList<>();
                                list.add(a1);
                                list.add(a2);

                                dbInstance.agentDao().insertAll(list);
                            }
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