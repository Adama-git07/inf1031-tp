package com.example.citoyenactif.ui.authentification;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.example.citoyenactif.R;
import com.example.citoyenactif.databinding.NfcWriteActivityBinding;
import com.example.citoyenactif.ui.viewmodel.NfcWriteViewModel;

import java.io.IOException;

/**
 * Écran "Programmer une carte d'agent".
 *
 * L'agent prouve son identité (numéro + mot de passe), puis approche une carte
 * vierge : l'Activity y écrit le message NDEF préparé par le ViewModel.
 * Écriture selon le cours : Ndef.get -> connect -> isWritable -> getMaxSize
 * -> writeNdefMessage -> close (dans finally).
 */
public class NfcWriteActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback {

    private NfcWriteActivityBinding binder;
    private NfcWriteViewModel viewModel;
    private NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // DataBinding : layout + ViewModel (double binding sur les champs)
        binder = DataBindingUtil.setContentView(this, R.layout.nfc_write_activity);
        viewModel = new ViewModelProvider(this).get(NfcWriteViewModel.class);
        binder.setViewModel(viewModel);
        binder.setLifecycleOwner(this);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (nfcAdapter == null) {
            viewModel.onWriteError("Ce téléphone n'a pas de NFC");
            return;
        }
        if (!nfcAdapter.isEnabled()) {
            viewModel.onWriteError("Le NFC est désactivé. Activez-le dans les paramètres.");
            return;
        }

        // On écoute les cartes tant que l'écran est visible
        int flags = NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_NFC_B
                | NfcAdapter.FLAG_READER_NFC_F | NfcAdapter.FLAG_READER_NFC_V;
        nfcAdapter.enableReaderMode(this, this, flags, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableReaderMode(this);
        }
    }

    /** Appelée (thread NFC) quand une carte est détectée. */
    @Override
    public void onTagDiscovered(Tag tag) {
        NdefMessage message = viewModel.getPendingMessage();
        if (message == null) {
            viewModel.onWriteError("Préparez d'abord la carte (identifiants + bouton)");
            return;
        }

        try {
            writeMessage(tag, message);
            viewModel.onWriteSuccess();
        } catch (IOException | FormatException e) {
            viewModel.onWriteError("Écriture impossible, gardez la carte contre le téléphone");
        } catch (IllegalStateException e) {
            viewModel.onWriteError(e.getMessage());
        }
    }

    /**
     * Écrit le message sur la carte.
     * Lance IllegalStateException avec un message clair si la carte ne convient pas.
     */
    private void writeMessage(Tag tag, NdefMessage message) throws IOException, FormatException {
        Ndef ndef = Ndef.get(tag);

        if (ndef != null) {
            // Carte déjà formatée NDEF
            try {
                ndef.connect();
                if (!ndef.isWritable()) {
                    throw new IllegalStateException("Cette carte est en lecture seule");
                }
                if (ndef.getMaxSize() < message.toByteArray().length) {
                    throw new IllegalStateException("Carte trop petite pour ce message");
                }
                ndef.writeNdefMessage(message);
            } finally {
                // Toujours fermer la connexion
                try {
                    ndef.close();
                } catch (IOException ignored) {
                }
            }
            return;
        }

        // Carte vierge : on la formate puis on écrit
        NdefFormatable formatable = NdefFormatable.get(tag);
        if (formatable == null) {
            throw new IllegalStateException("Carte non compatible NDEF");
        }
        try {
            formatable.connect();
            formatable.format(message);
        } finally {
            try {
                formatable.close();
            } catch (IOException ignored) {
            }
        }
    }
}