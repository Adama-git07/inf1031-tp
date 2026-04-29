package com.example.citoyenactif.data.repository;

import com.example.citoyenactif.CitoyenActifApp;
import com.example.citoyenactif.data.local.dao.UserDao;
import com.example.citoyenactif.data.local.entity.UserEntity;
import com.example.citoyenactif.model.dto.UserCreateDTO;
import com.example.citoyenactif.model.enums.UserRoles;

/**
 * Repository responsable de la gestion des utilisateurs.
 *
 * Intermédiaire entre la couche UI et la base de données Room.
 */
public class UserRepository {

    private final UserDao userDao;

    public UserRepository() {
        userDao = CitoyenActifApp.getInstance().getDatabase().userDao();
    }

    /**
     * Enregistre un nouvel utilisateur.
     * Lit les ObservableField du DTO via .get().
     *
     * @param dto données du formulaire d'inscription
     * @return id du nouvel utilisateur inséré
     */
    public long register(UserCreateDTO dto) {

        UserEntity user = new UserEntity();

        // Lecture des ObservableField (null-safe)
        user.lastName  = dto.lastName.get()  != null ? dto.lastName.get().trim()  : "";
        user.firstName = dto.firstName.get() != null ? dto.firstName.get().trim() : "";
        user.address   = dto.address.get()   != null ? dto.address.get().trim()   : "";
        user.email     = dto.email.get()     != null ? dto.email.get().trim()     : "";
        user.phone     = dto.phone.get()     != null ? dto.phone.get().trim()     : "";
        user.username  = dto.username.get()  != null ? dto.username.get().trim()  : "";
        user.password  = dto.password.get()  != null ? dto.password.get().trim()  : "";

        user.role      = (dto.role != null) ? dto.role : UserRoles.CITIZEN;
        user.createdAt = System.currentTimeMillis();

        return userDao.insert(user);
    }

    /**
     * Vérifie les identifiants de connexion.
     *
     * @param login    username ou email
     * @param password mot de passe
     * @return UserEntity si valide, null sinon
     */
    public UserEntity login(String login, String password) {
        return userDao.login(login, password);
    }

    /**
     * Réinitialise le mot de passe par username.
     */
    public boolean resetPasswordByUsername(String username, String newPassword) {
        int rows = userDao.updatePasswordByUsername(username, newPassword);
        return rows > 0;
    }

    /**
     * Récupère un utilisateur par son ID.
     */
    public UserEntity getById(long id) {
        return userDao.getById(id);
    }
}
