package com.example.citoyenactif.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.citoyenactif.data.local.entity.AgentEntity;
import com.example.citoyenactif.data.repository.AgentRepository;

/**
 * ViewModel du profil agent municipal.
 *
 * Charge l'agent connecté depuis Room dans un thread secondaire
 * et expose le résultat via un LiveData observable.
 *
 * Utilisé par : AgentProfilFragment
 */
public class AgentProfileViewModel extends AndroidViewModel {

    private final AgentRepository agentRepo;

    /** Données de l'agent connecté (null si non trouvé). */
    private final MutableLiveData<AgentEntity> agent = new MutableLiveData<>();

    public AgentProfileViewModel(@NonNull Application application) {
        super(application);
        agentRepo = new AgentRepository();
    }

    /** Retourne le LiveData de l'agent à observer dans le Fragment. */
    public LiveData<AgentEntity> getAgent() {
        return agent;
    }

    /**
     * Charge l'agent par son ID depuis Room (thread secondaire).
     * Poste le résultat (ou null si introuvable) dans le LiveData agent.
     *
     * @param agentId identifiant de l'agent connecté (depuis SessionManager)
     */
    public void loadAgent(long agentId) {
        new Thread(() -> {
            AgentEntity a = agentRepo.getById(agentId);
            agent.postValue(a);
        }).start();
    }
}
