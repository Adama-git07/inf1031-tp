package com.example.citoyenactif.ui.authentification;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.example.citoyenactif.R;
import com.example.citoyenactif.databinding.NfcScanActivityBinding;
import com.example.citoyenactif.ui.municipal.MunicipalPageActivity;
import com.example.citoyenactif.ui.viewmodel.NfcScanViewModel;
import com.example.citoyenactif.util.SessionManager;

import java.io.IOException;

/**
 * Écran "Activer mon compte" : l'agent approche sa carte NFC pour se connecter,
 * sans mot de passe.
 *
 * On utilise le Reader Mode (recommandé dans le cours pour un écran de scan dédié) :
 * - activé dans onResume, désactivé dans onPause ;
 * - la lecture se fait dans un thread NFC, jamais sur le thread de l'interface.
 */
public class NfcScanActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback {

    private NfcScanActivityBinding binder;
    private NfcScanViewModel viewModel;
    private NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // DataBinding : on lie le layout et le ViewModel
        binder = DataBindingUtil.setContentView(this, R.layout.nfc_scan_activity);
        viewModel = new ViewModelProvider(this).get(NfcScanViewModel.class);
        binder.setViewModel(viewModel);
        binder.setLifecycleOwner(this);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        binder.btnNfcSettings.setOnClickListener(v -> openNfcSettings());

        // Quand une carte valide est reconnue : on ouvre la session et on va à l'écran agent
        viewModel.getAgent().observe(this, agent -> {
            if (agent != null) {
                SessionManager.saveAgent(this, agent.id);
                Intent intent = new Intent(this, MunicipalPageActivity.class);
                // On vide la pile pour que "Retour" ne revienne pas à la connexion
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Cas 1 : le téléphone n'a pas de NFC
        if (nfcAdapter == null) {
            viewModel.onCardError("Ce téléphone n'a pas de NFC");
            binder.btnNfcSettings.setVisibility(View.GONE);
            return;
        }

        // Cas 2 : NFC désactivé -> on propose d'ouvrir les paramètres
        // (une app n'a pas le droit de l'activer toute seule)
        if (!nfcAdapter.isEnabled()) {
            viewModel.onCardError("Le NFC est désactivé. Activez-le puis revenez ici.");
            binder.btnNfcSettings.setVisibility(View.VISIBLE);
            return;
        }

        // Cas 3 : tout va bien -> on écoute les cartes
        binder.btnNfcSettings.setVisibility(View.GONE);
        int flags = NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_NFC_B
                | NfcAdapter.FLAG_READER_NFC_F | NfcAdapter.FLAG_READER_NFC_V;
        nfcAdapter.enableReaderMode(this, this, flags, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // On arrête d'écouter quand l'écran n'est plus au premier plan
        if (nfcAdapter != null) {
            nfcAdapter.disableReaderMode(this);
        }
    }

    /** Appelée par Android (thread NFC) quand une carte est détectée. */
    @Override
    public void onTagDiscovered(Tag tag) {
        Ndef ndef = Ndef.get(tag);
        if (ndef == null) {
            viewModel.onCardError("Carte non compatible");
            return;
        }
        try {
            ndef.connect();
            NdefMessage message = ndef.getNdefMessage();
            viewModel.onCardRead(message);
        } catch (IOException | FormatException e) {
            // Par exemple : carte retirée trop vite
            viewModel.onCardError("Lecture impossible, réessayez");
        } finally {
            // Toujours fermer la connexion à la carte
            try {
                ndef.close();
            } catch (IOException ignored) {
            }
        }
    }

    /** Ouvre les paramètres NFC (ou les paramètres sans fil si indisponible). */
    private void openNfcSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
        }
    }
}