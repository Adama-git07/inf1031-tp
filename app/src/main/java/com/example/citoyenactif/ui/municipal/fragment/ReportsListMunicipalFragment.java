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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.citoyenactif.data.local.entity.ReportEntity;
import com.example.citoyenactif.data.repository.ReportRepository;
import com.example.citoyenactif.databinding.ReportsListMunicipalFragmentBinding;
import com.example.citoyenactif.model.enums.ReportStatus;
import com.example.citoyenactif.ui.municipal.adapter.ReportAdapter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReportsListMunicipalFragment extends Fragment {

    private ReportsListMunicipalFragmentBinding binder;

    private ReportRepository repo;
    private ReportAdapter adapter;

    // Camera flow
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private Uri pendingPhotoUri;
    private ReportEntity pendingResolveReport;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        binder = ReportsListMunicipalFragmentBinding.inflate(inflater, container, false);

        // Repository (ton constructor prend Application)
        repo = new ReportRepository(requireActivity().getApplication());

        // RecyclerView
        adapter = new ReportAdapter(this::showStatusDialog);
        binder.rvReports.setLayoutManager(new LinearLayoutManager(getContext()));
        binder.rvReports.setAdapter(adapter);

        // LiveData => refresh auto
        repo.getAllReports().observe(getViewLifecycleOwner(), reports -> adapter.submit(reports));

        // Launcher caméra: prend une photo et l’écrit dans pendingPhotoUri
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (Boolean.TRUE.equals(success)
                            && pendingResolveReport != null
                            && pendingPhotoUri != null) {

                        // Update BD : RESOLU + preuve
                        repo.resolveWithProof(pendingResolveReport.id, pendingPhotoUri.toString());

                        Toast.makeText(getContext(), "Report marqué RESOLU (preuve enregistrée)", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Photo annulée : statut non modifié", Toast.LENGTH_SHORT).show();
                    }

                    pendingResolveReport = null;
                    pendingPhotoUri = null;
                }
        );

        return binder.getRoot();
    }

    private void showStatusDialog(ReportEntity report) {
        if (report == null) return;

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

                    // Cas spécial : RESOLU => photo caméra obligatoire
                    if (chosen == ReportStatus.RESOLU) {
                        pendingResolveReport = report;
                        pendingPhotoUri = createImageUriInFiles();

                        if (pendingPhotoUri == null) {
                            Toast.makeText(getContext(), "Impossible de préparer la caméra", Toast.LENGTH_SHORT).show();
                            pendingResolveReport = null;
                            dialog.dismiss();
                            return;
                        }

                        takePictureLauncher.launch(pendingPhotoUri);
                        dialog.dismiss();
                        return;
                    }

                    // Autres statuts : update simple
                    repo.updateStatus(report.id, chosen.name());
                    Toast.makeText(getContext(), "Statut mis à jour : " + chosen.name(), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .setNegativeButton("Annuler", (d, w) -> d.dismiss())
                .show();
    }

    /**
     * Crée une URI FileProvider pour enregistrer une photo dans /files/images/
     * Compatible avec file_paths.xml utilisant <files-path name="images" path="images/" />
     */
    private Uri createImageUriInFiles() {
        try {
            File dir = new File(requireContext().getFilesDir(), "images"); //  files-path
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