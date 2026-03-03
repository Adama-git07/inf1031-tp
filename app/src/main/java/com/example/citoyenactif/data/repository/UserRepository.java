package com.example.citoyenactif.data.repository;

import android.content.Context;

import com.example.citoyenactif.data.local.dao.UserDao;
import com.example.citoyenactif.data.local.database.DatabaseClient;
import com.example.citoyenactif.data.local.entity.UserEntity;
import com.example.citoyenactif.model.dto.UserCreateDTO;
import com.example.citoyenactif.model.enums.UserRoles;


/**
 * Repository responsable de la gestion des utilisateurs.
 *
 * Cette classe sert d'intermédiaire entre :
 * - La couche UI (Fragments / Activities)
 * - La couche Data (Room / DAO)
 *
 * Elle permet d'isoler la logique d'accès aux données.
 */
public class UserRepository {

    /**
     * DAO permettant d'exécuter les opérations
     * sur la table "users".
     */
    private final UserDao userDao;

    /**
     * Constructeur du Repository.
     *
     * Initialise l'accès à la base de données via le DatabaseClient.
     *
     * @param context contexte de l'application
     */
    public UserRepository(Context context){
        userDao = DatabaseClient
                .getInstance(context)
                .getDb()
                .userDao();
    }

    /**
     * Enregistre un nouvel utilisateur dans la base de données.
     *
     * Convertit le DTO (données UI) en UserEntity (modèle Room).
     *
     * @param dto données saisies lors de la création de compte
     * @return id de l'utilisateur inséré
     */
    public long register(UserCreateDTO dto){

        // Création d'une nouvelle entité utilisateur
        UserEntity user = new UserEntity();
        user.username = dto.username;
        user.email = dto.email;
        user.password = dto.password;

        // Gestion du rôle via l'énumération
        if (dto.role == null){
            user.role = UserRoles.CITIZEN;      // Rôle par défaut
        } else {
            user.role = UserRoles.valueOf(String.valueOf(dto.role));
        }
        user.createdAt = System.currentTimeMillis();    // Date de création (timestamp actuel)

        return userDao.insert(user);        // Insertion en base via Room
    }

    /**
     * Vérifie les identifiants d'un utilisateur.
     *
     * @param login peut être username ou email
     * @param password mot de passe
     * @return UserEntity si les identifiants sont valides, sinon null
     */
    public UserEntity login(String login, String password){
        return userDao.login(login,password);
    }
}
