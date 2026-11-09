package com.example.citoyenactif.ui.viewmodel;

import android.app.Application;
import android.nfc.NdefMessage;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.citoyenactif.data.local.entity.AgentEntity;
import com.example.citoyenactif.data.repository.AgentRepository;
import com.example.citoyenactif.util.NfcCardUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel de l'écran "Activer mon compte avec ma carte".
 *
 * - Reçoit le message lu sur la carte (depuis l'Activity).
 * - Vérifie la carte, cherche l'agent dans Room (thread secondaire).
 * - Expose un message d'état (lié au layout par DataBinding)
 *   et l'agent trouvé (pour ouvrir sa session).
 */
public class NfcScanViewModel extends AndroidViewModel {

    private final AgentRepository agentRepo = new AgentRepository();

    /** Un seul thread d'arrière-plan pour les accès Room. */
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    /** Texte affiché à l'écran (lié au XML avec @{viewModel.status}). */
    private final MutableLiveData<String> status =
            new MutableLiveData<>("Approchez votre carte d'agent du dos du téléphone");

    /** Agent reconnu par la carte (null tant qu'aucune carte valide). */
    private final MutableLiveData<AgentEntity> agent = new MutableLiveData<>();

    public NfcScanViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<String> getStatus() { return status; }
    public LiveData<AgentEntity> getAgent() { return agent; }

    /** Appelée quand une carte a été lue (peut venir d'un thread NFC). */
    public void onCardRead(NdefMessage message) {
        String agentNumber = NfcCardUtil.extractAgentNumber(message);
        if (agentNumber == null) {
            status.postValue("Carte non reconnue ou invalide");
            return;
        }
        executor.execute(() -> {
            AgentEntity found = agentRepo.findByNumber(agentNumber);
            if (found == null) {
                status.postValue("Aucun agent ne correspond à cette carte");
            } else {
                status.postValue("Bienvenue " + found.firstName + " !");
                agent.postValue(found);
            }
        });
    }

    /** Appelée quand la lecture échoue (carte retirée trop vite, vide, etc.). */
    public void onCardError(String message) {
        status.postValue(message);
    }

    @Override
    protected void onCleared() {
        executor.shutdownNow();
    }
}