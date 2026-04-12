package com.example.citoyenactif.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.citoyenactif.data.local.entity.AgentEntity;
import com.example.citoyenactif.data.local.entity.UserEntity;
import com.example.citoyenactif.data.repository.AgentRepository;
import com.example.citoyenactif.data.repository.UserRepository;
import com.example.citoyenactif.model.dto.UserCreateDTO;

/**
 * ViewModel d'authentification.
 *
 * Centralise toutes les opérations liées à l'authentification :
 *   - Connexion citoyen (login + mot de passe)
 *   - Connexion agent (numéro d'agent + mot de passe)
 *   - Inscription citoyen
 *   - Réinitialisation du mot de passe
 *
 * Chaque opération Room est exécutée dans un thread secondaire.
 * Le résultat est posté via MutableLiveData et observé depuis le Fragment.
 *
 * Utilisé par :
 *   - SignInFrag
 *   - SignUpFrag
 *   - ResetPasswordFrag
 */
public class AuthViewModel extends AndroidViewModel {

    private final UserRepository  userRepo;
    private final AgentRepository agentRepo;



    /** Résultat de la connexion citoyen : UserEntity si succès, null si échec. */
    private final MutableLiveData<UserEntity> citizenLoginResult = new MutableLiveData<>();

    /** Résultat de la connexion agent : AgentEntity si succès, null si échec. */
    private final MutableLiveData<AgentEntity> agentLoginResult = new MutableLiveData<>();

    /** Résultat de l'inscription : true si succès, false si erreur (doublon). */
    private final MutableLiveData<Boolean> registerResult = new MutableLiveData<>();

    /** Résultat de la réinitialisation du mot de passe. */
    private final MutableLiveData<Boolean> resetPasswordResult = new MutableLiveData<>();

    /** Message d'erreur générique (ex. doublon d'email à l'inscription). */
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();



    public AuthViewModel(@NonNull Application application) {
        super(application);
        userRepo  = new UserRepository();
        agentRepo = new AgentRepository();
    }



    public LiveData<UserEntity>  getCitizenLoginResult()   { return citizenLoginResult; }
    public LiveData<AgentEntity> getAgentLoginResult()     { return agentLoginResult; }
    public LiveData<Boolean>     getRegisterResult()       { return registerResult; }
    public LiveData<Boolean>     getResetPasswordResult()  { return resetPasswordResult; }
    public LiveData<String>      getErrorMessage()         { return errorMessage; }



    /**
     * Connexion citoyen via username/email + mot de passe.
     * Poste null dans citizenLoginResult si les identifiants sont incorrects.
     */
    public void loginCitizen(String login, String password) {
        new Thread(() -> {
            UserEntity user = userRepo.login(login, password);
            citizenLoginResult.postValue(user);
        }).start();
    }

    /**
     * Connexion agent via numéro d'agent + mot de passe.
     * Poste null dans agentLoginResult si les identifiants sont incorrects.
     */
    public void loginAgent(String agentNumber, String password) {
        new Thread(() -> {
            AgentEntity agent = agentRepo.login(agentNumber, password);
            agentLoginResult.postValue(agent);
        }).start();
    }

    /**
     * Inscription d'un nouveau citoyen.
     * Poste true dans registerResult si l'inscription réussit.
     * Poste false + un message dans errorMessage en cas de doublon.
     */
    public void register(UserCreateDTO dto) {
        new Thread(() -> {
            try {
                userRepo.register(dto);
                registerResult.postValue(true);
            } catch (Exception e) {
                errorMessage.postValue("Erreur : nom d'utilisateur ou courriel déjà utilisé");
                registerResult.postValue(false);
            }
        }).start();
    }

    /**
     * Réinitialise le mot de passe d'un utilisateur identifié par son username.
     * Poste true si la mise à jour a réussi (utilisateur trouvé), false sinon.
     */
    public void resetPassword(String username, String newPassword) {
        new Thread(() -> {
            boolean success = userRepo.resetPasswordByUsername(username, newPassword);
            resetPasswordResult.postValue(success);
        }).start();
    }
}
