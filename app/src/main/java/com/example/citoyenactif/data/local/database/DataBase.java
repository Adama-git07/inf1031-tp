package com.example.citoyenactif.data.local.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.citoyenactif.data.local.converter.UserRoleConverter;
import com.example.citoyenactif.data.local.dao.AgentDao;
import com.example.citoyenactif.data.local.dao.ReportDao;
import com.example.citoyenactif.data.local.dao.UserDao;
import com.example.citoyenactif.data.local.entity.AgentEntity;
import com.example.citoyenactif.data.local.entity.UserEntity;
import com.example.citoyenactif.data.local.converter.ReportCategoryConverter;
import com.example.citoyenactif.data.local.converter.ReportStatusConverter;
import com.example.citoyenactif.data.local.entity.ReportEntity;


/**
 * Classe principale représentant la base de données Room
 * de l'application Citoyen Actif.
 *
 * Cette classe :
 * - Déclare les entités (tables)
 * - Associe les DAO
 * - Configure les convertisseurs personnalisés
 *
 * Elle étend RoomDatabase, ce qui permet à Room
 * de générer automatiquement son implémentation interne.
 */
@Database(entities = {UserEntity.class, AgentEntity.class, ReportEntity.class}, // Liste des tables de la base
        version = 4                    // v4 : ajout firstName, lastName, phone, address dans users
)
@TypeConverters({UserRoleConverter.class, ReportStatusConverter.class, ReportCategoryConverter.class})  // Permet d'utiliser l'enum UserRole
public abstract class DataBase extends RoomDatabase {

    /**
     * Fournit l'accès aux opérations liées aux utilisateurs.
     *
     * Room génère automatiquement l'implémentation
     * de cette méthode au moment de la compilation.
     *
     * @return instance du UserDao
     */
    public abstract UserDao userDao();

    public abstract  AgentDao agentDao();

    public abstract ReportDao reportDao();
}
