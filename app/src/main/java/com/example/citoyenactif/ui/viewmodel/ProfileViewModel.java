package com.example.citoyenactif.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.citoyenactif.data.local.entity.UserEntity;
import com.example.citoyenactif.data.repository.UserRepository;

/**
 * ViewModel du profil citoyen.
 *
 * Charge l'utilisateur connecté depuis Room dans un thread secondaire
 * et expose le résultat via un LiveData observable.
 *
 * Utilisé par : ProfilFragment
 */
public class ProfileViewModel extends AndroidViewModel {

    private final UserRepository userRepo;

    /** Données de l'utilisateur connecté (null si non trouvé). */
    private final MutableLiveData<UserEntity> user = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        userRepo = new UserRepository(application);
    }

    /** Retourne le LiveData de l'utilisateur à observer dans le Fragment. */
    public LiveData<UserEntity> getUser() {
        return user;
    }

    /**
     * Charge l'utilisateur par son ID depuis Room (thread secondaire).
     * Poste le résultat (ou null si introuvable) dans le LiveData user.
     *
     * @param userId identifiant de l'utilisateur connecté (depuis SessionManager)
     */
    public void loadUser(long userId) {
        new Thread(() -> {
            UserEntity u = userRepo.getById(userId);
            user.postValue(u);
        }).start();
    }
}
