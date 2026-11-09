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
 * ViewModel de l'écran "Programmer une carte d'agent".
 *
 * Étapes :
 * 1) L'agent saisit son numéro et son mot de passe (double binding avec les champs).
 * 2) prepareCard() vérifie les identifiants dans Room (thread secondaire).
 * 3) Si c'est bon, le message NDEF est prêt : l'Activity l'écrit quand une carte est présentée.
 */
public class NfcWriteViewModel extends AndroidViewModel {

    private final AgentRepository agentRepo = new AgentRepository();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    /** Champs de saisie : liés aux EditText avec @={...} (double binding). */
    private final MutableLiveData<String> agentNumber = new MutableLiveData<>("");
    private final MutableLiveData<String> password = new MutableLiveData<>("");

    /** Message affiché à l'écran. */
    private final MutableLiveData<String> status = new MutableLiveData<>(
            "Saisissez vos identifiants, puis appuyez sur « Préparer la carte »");

    /** true quand les identifiants sont valides et que le message est prêt à écrire. */
    private final MutableLiveData<Boolean> ready = new MutableLiveData<>(false);

    /** Message à écrire sur la carte (null tant que rien n'est prêt). */
    private volatile NdefMessage pendingMessage;

    public NfcWriteViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<String> getAgentNumber() { return agentNumber; }
    public MutableLiveData<String> getPassword() { return password; }
    public LiveData<String> getStatus() { return status; }
    public LiveData<Boolean> getReady() { return ready; }
    public NdefMessage getPendingMessage() { return pendingMessage; }

    /** Appelée par le bouton (binding d'action dans le XML). */
    public void prepareCard() {
        String number = agentNumber.getValue() == null ? "" : agentNumber.getValue().trim();
        String pwd = password.getValue() == null ? "" : password.getValue().trim();

        if (number.isEmpty() || pwd.isEmpty()) {
            status.setValue("Veuillez entrer le numéro et le mot de passe");
            return;
        }

        executor.execute(() -> {
            AgentEntity agent = agentRepo.login(number, pwd);
            if (agent == null) {
                pendingMessage = null;
                ready.postValue(false);
                status.postValue("Numéro d'agent ou mot de passe incorrect");
                return;
            }
            try {
                pendingMessage = NfcCardUtil.buildMessage(agent.agentNumber);
                ready.postValue(true);
                status.postValue("Identifiants OK. Approchez une carte vierge du téléphone.");
            } catch (IllegalArgumentException e) {
                status.postValue("Ce numéro d'agent ne peut pas être écrit sur une carte");
            }
        });
    }

    /** Appelée par l'Activity quand l'écriture a réussi. */
    public void onWriteSuccess() {
        pendingMessage = null;
        ready.postValue(false);
        password.postValue("");
        status.postValue("Carte programmée avec succès !");
    }

    /** Appelée par l'Activity quand l'écriture a échoué. */
    public void onWriteError(String message) {
        status.postValue(message);
    }

    @Override
    protected void onCleared() {
        executor.shutdownNow();
    }
}