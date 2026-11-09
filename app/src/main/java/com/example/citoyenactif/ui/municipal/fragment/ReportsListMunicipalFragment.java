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

import com.example.citoyenactif.data.local.entity.ReportEntity;
import com.example.citoyenactif.databinding.DialogReportDetailBinding;
import com.example.citoyenactif.databinding.ReportsListMunicipalFragmentBinding;
import com.example.citoyenactif.model.enums.ReportStatus;
import com.example.citoyenactif.ui.binding.ReportEditModel;
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
 * - Affiche les signalements dans un RecyclerView (ReportAdapter)
 * - Un clic ouvre un dialogue de détail (DataBinding : dialog_report_detail.xml)
 * - L'agent peut passer le statut à EN_COURS, RESOLU (photo obligatoire) ou IGNORE (raison)
 */
public class ReportsListMunicipalFragment extends Fragment {

    /** Binding du layout reports_list_municipal_fragment.xml */
    private ReportsListMunicipalFragmentBinding binder;

    /** ViewModel partagé pour les rapports */
    private ReportViewModel viewModel;

    /** Adapter du RecyclerView */
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

        viewModel = new ViewModelProvider(this).get(ReportViewModel.class);

        // RecyclerView + Adapter : un clic ouvre le dialogue de détail
        adapter = new ReportAdapter(this::showDetailDialog);
        binder.rvReports.setLayoutManager(new LinearLayoutManager(getContext()));
        binder.rvReports.setAdapter(adapter);

        // Observation LiveData : mise à jour automatique de la liste
        viewModel.getAllReports().observe(getViewLifecycleOwner(),
                reports -> adapter.submit(reports));

        // Launcher caméra (preuve RESOLU)
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
     * Ouvre le dialogue de détail d'un signalement.
     * Le XML (dialog_report_detail.xml) affiche les données et gère les clics
     * grâce au modèle ReportEditModel et aux actions définies ici.
     */
    private void showDetailDialog(ReportEntity report) {
        if (report == null) return;

        // Un signalement résolu n'est plus modifiable
        if (report.status == ReportStatus.RESOLU) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Rapport résolu")
                    .setMessage("Ce rapport a été marqué comme résolu et ne peut plus être modifié.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        // Binding du dialogue + modèle observable
        DialogReportDetailBinding b = DialogReportDetailBinding.inflate(
                LayoutInflater.from(requireContext()));
        ReportEditModel model = new ReportEditModel(report);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(b.getRoot())
                .setNegativeButton("Fermer", null)
                .create();

        b.setModel(model);
        b.setActions(new ReportEditModel.Actions() {

            @Override
            public void onInProgress() {
                viewModel.updateStatus(report.id, ReportStatus.EN_COURS.name());
                Toast.makeText(getContext(), "Statut mis à jour : EN_COURS",
                        Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }

            @Override
            public void onResolve() {
                // RESOLU : la photo de preuve est obligatoire
                pendingResolveReport = report;
                pendingPhotoUri = createImageUriInFiles();

                if (pendingPhotoUri == null) {
                    Toast.makeText(getContext(), "Impossible de préparer la caméra",
                            Toast.LENGTH_SHORT).show();
                    pendingResolveReport = null;
                    return;
                }
                dialog.dismiss();
                takePictureLauncher.launch(pendingPhotoUri);
            }

            @Override
            public void onIgnore(String reason) {
                // La raison vient du modèle (double binding)
                String clean = reason == null ? "" : reason.trim();
                if (clean.isEmpty()) {
                    Toast.makeText(getContext(), "Veuillez saisir une raison",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                long agentId = SessionManager.getUserId(requireContext());
                viewModel.ignoreWithReason(report.id, clean, agentId);
                Toast.makeText(getContext(), "Rapport ignoré", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.show();
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