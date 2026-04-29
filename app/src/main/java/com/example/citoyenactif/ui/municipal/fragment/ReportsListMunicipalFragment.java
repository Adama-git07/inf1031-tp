package com.example.citoyenactif.ui.municipal.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.widget.EditText;

import com.example.citoyenactif.data.local.entity.ReportEntity;
import com.example.citoyenactif.databinding.ReportsListMunicipalFragmentBinding;
import com.example.citoyenactif.model.enums.ReportStatus;
import com.example.citoyenactif.ui.municipal.adapter.ReportAdapter;
import com.example.citoyenactif.ui.viewmodel.ReportViewModel;
import com.example.citoyenactif.util.SessionManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Fragment de la partie municipale affichant la liste des signalements.
 *
 * Responsabilités :
 * - Afficher les signalements dans un RecyclerView via ReportAdapter
 * - Observer les données (LiveData) pour mettre la liste à jour automatiquement
 * - Permettre à l'agent de changer le statut d'un signalement
 * - Si l'agent choisit le statut RESOLU : obliger la prise d'une photo (preuve)
 *
 * Architecture MVVM :
 *   - ReportViewModel gère toutes les opérations Room.
 *   - Le Fragment observe le LiveData et délègue les écritures au ViewModel.
 *   - Aucun accès direct au repository dans le Fragment.
 */
public class ReportsListMunicipalFragment extends Fragment {

    /** Binding du layout reports_list_municipal_fragment.xml */
    private ReportsListMunicipalFragmentBinding binder;

    /** ViewModel partagé pour les rapports */
    private ReportViewModel viewModel;

    /** Adapter du RecyclerView pour afficher les signalements */
    private ReportAdapter adapter;

    // ── Flow caméra (preuve RESOLU) ───────────────────────────────────────────

    /** Launcher caméra (TakePicture) */
    private ActivityResultLauncher<Uri> takePictureLauncher;

    /** URI temporaire du fichier photo à écrire */
    private Uri pendingPhotoUri;

    /** Signalement dont on veut marquer le statut RESOLU */
    private ReportEntity pendingResolveReport;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        binder = ReportsListMunicipalFragmentBinding.inflate(inflater, container, false);

        // Obtenir le ViewModel
        viewModel = new ViewModelProvider(this).get(ReportViewModel.class);

        // ── RecyclerView + Adapter ─────────────────────────────────────────────
        adapter = new ReportAdapter(this::showStatusDialog);
        binder.rvReports.setLayoutManager(new LinearLayoutManager(getContext()));
        binder.rvReports.setAdapter(adapter);

        // Observation LiveData — mise à jour automatique à chaque changement Room
        viewModel.getAllReports().observe(getViewLifecycleOwner(),
                reports -> adapter.submit(reports));

        // ── Launcher caméra (preuve RESOLU) ────────────────────────────────────
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (Boolean.TRUE.equals(success)
                            && pendingResolveReport != null
                            && pendingPhotoUri != null) {

                        long agentId = SessionManager.getUserId(requireContext());
                        viewModel.resolveWithProof(
                                pendingResolveReport.id,
                                pendingPhotoUri.toString(),
                                agentId
                        );

                        Toast.makeText(getContext(),
                                "Report marqué RESOLU (preuve enregistrée)",
                                Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(getContext(),
                                "Photo annulée : statut non modifié",
                                Toast.LENGTH_SHORT).show();
                    }

                    pendingResolveReport = null;
                    pendingPhotoUri = null;
                }
        );

        return binder.getRoot();
    }

    /**
     * Affiche un dialog permettant à l'agent de choisir un statut pour un signalement.
     *
     * - Si le statut choisi est RESOLU : force la prise d'une photo (preuve).
     * - Sinon : simple mise à jour du statut via le ViewModel.
     */
    private void showStatusDialog(ReportEntity report) {
        if (report == null) return;

        if (report.status == ReportStatus.RESOLU) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Rapport résolu")
                    .setMessage("Ce rapport a été marqué comme résolu et ne peut plus être modifié.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        ReportStatus[] statuses = ReportStatus.values();
        String[] labels = new String[statuses.length];
        int checked = -1;

        for (int i = 0; i < statuses.length; i++) {
            labels[i] = statuses[i].name();
            if (report.status == statuses[i]) checked = i;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Changer le statut")
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {

                    ReportStatus chosen = statuses[which];

                    if (chosen == ReportStatus.RESOLU) {
                        pendingResolveReport = report;
                        pendingPhotoUri = createImageUriInFiles();

                        if (pendingPhotoUri == null) {
                            Toast.makeText(getContext(),
                                    "Impossible de préparer la caméra",
                                    Toast.LENGTH_SHORT).show();
                            pendingResolveReport = null;
                            dialog.dismiss();
                            return;
                        }

                        takePictureLauncher.launch(pendingPhotoUri);
                        dialog.dismiss();
                        return;
                    }

                    if (chosen == ReportStatus.IGNORE) {
                        dialog.dismiss();
                        showIgnoreReasonDialog(report);
                        return;
                    }

                    // Autres statuts : délégation au ViewModel
                    viewModel.updateStatus(report.id, chosen.name());

                    Toast.makeText(getContext(),
                            "Statut mis à jour : " + chosen.name(),
                            Toast.LENGTH_SHORT).show();

                    dialog.dismiss();
                })
                .setNegativeButton("Annuler", (d, w) -> d.dismiss())
                .show();
    }

    /**
     * Affiche un dialog pour saisir la raison d'ignorance avant de confirmer.
     */
    private void showIgnoreReasonDialog(ReportEntity report) {
        EditText etReason = new EditText(requireContext());
        etReason.setHint("Raison (ex : doublon, faux rapport…)");
        etReason.setPadding(48, 24, 48, 24);

        new AlertDialog.Builder(requireContext())
                .setTitle("Ignorer le rapport")
                .setMessage("Veuillez indiquer la raison :")
                .setView(etReason)
                .setPositiveButton("Confirmer", (d, w) -> {
                    String reason = etReason.getText().toString().trim();
                    if (reason.isEmpty()) {
                        Toast.makeText(getContext(),
                                "Veuillez saisir une raison", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    long agentId = SessionManager.getUserId(requireContext());
                    viewModel.ignoreWithReason(report.id, reason, agentId);
                    Toast.makeText(getContext(),
                            "Rapport ignoré", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    /**
     * Crée une URI via FileProvider pour enregistrer une photo dans files/images/.
     *
     * @return Uri sécurisée (FileProvider) ou null si échec
     */
    private Uri createImageUriInFiles() {
        try {
            File dir = new File(requireContext().getFilesDir(), "images");
            if (!dir.exists()) dir.mkdirs();

            String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File image = File.createTempFile("RESOLVE_" + ts + "_", ".jpg", dir);

            return FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    image
            );
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binder = null;
    }
}
