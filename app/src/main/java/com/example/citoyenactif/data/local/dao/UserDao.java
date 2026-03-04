package com.example.citoyenactif.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.citoyenactif.data.local.entity.UserEntity;

/**
 * Data Access Object (DAO) pour l'entité UserEntity.
 *
 * Cette interface définit toutes les opérations possibles
 * sur la table "users" dans la base de données Room.
 *
 * Room génère automatiquement l’implémentation de ces méthodes
 * lors de la compilation.
 */
@Dao
public interface UserDao {
    /**
     * Insère un nouvel utilisateur dans la base de données.
     *
     * @param user objet UserEntity à insérer
     * @return l'identifiant (id) auto-généré de l'utilisateur inséré
     *
     * OnConflictStrategy.ABORT :
     * - Si une contrainte est violée (ex : username ou email déjà existant),
     *   l'insertion est annulée et une exception est levée.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(UserEntity user);


    /**
     * Recherche un utilisateur à partir de son nom d'utilisateur.
     *
     * @param username nom d'utilisateur à rechercher
     * @return UserEntity correspondant si trouvé, sinon null
     *
     * LIMIT 1 :
     * - Permet de ne récupérer qu’un seul résultat.
     */
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    UserEntity findByUsername(String username);


    /**
     * Recherche un utilisateur à partir de son adresse email.
     *
     * @param email adresse email à rechercher
     * @return UserEntity correspondant si trouvé, sinon null
     */
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    UserEntity findByEmail(String email);


    /**
     * Vérifie les identifiants de connexion d’un utilisateur.
     *
     * @param login peut être soit le nom d'utilisateur, soit l'email
     * @param password mot de passe saisi par l'utilisateur
     * @return UserEntity si les identifiants sont valides, sinon null
     *
     * La requête permet :
     * - De se connecter avec username OU email
     * - De vérifier que le mot de passe correspond
     */
    @Query("SELECT * FROM users WHERE (username = :login OR email = :login) AND password = :password LIMIT 1")
    UserEntity login(String login, String password);

    /**
     * Met à jour le mot de passe d'un utilisateur à partir de son username.
     *
     * @param username nom d'utilisateur
     * @param newPassword nouveau mot de passe
     * @return nombre de lignes modifiées (0 = utilisateur introuvable, 1 = succès)
     */
    @Query("UPDATE users SET password = :newPassword WHERE username = :username")
    int updatePasswordByUsername(String username, String newPassword);
}
