package com.example.citoyenactif.ui.municipal.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.citoyenactif.data.local.entity.ReportEntity;
import com.example.citoyenactif.data.repository.ReportRepository;
import com.example.citoyenactif.databinding.ReportsListMunicipalFragmentBinding;
import com.example.citoyenactif.model.enums.ReportStatus;
import com.example.citoyenactif.ui.municipal.adapter.ReportAdapter;

public class ReportsListMunicipalFragment extends Fragment {

    private ReportsListMunicipalFragmentBinding binder;
    private ReportAdapter adapter;
    private ReportRepository repo;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        binder = ReportsListMunicipalFragmentBinding.inflate(inflater, container, false);

        repo = new ReportRepository(requireActivity().getApplication());

        adapter = new ReportAdapter(this::showStatusDialog);
        binder.rvReports.setLayoutManager(new LinearLayoutManager(getContext()));
        binder.rvReports.setAdapter(adapter);

        repo.getAllReports().observe(getViewLifecycleOwner(), reports -> adapter.submit(reports));

        return binder.getRoot();
    }

    private void showStatusDialog(ReportEntity report) {
        if (report == null) return;

        // Liste des statuts possibles
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

                    // Update en BD (LiveData refresh automatiquement)
                    repo.updateStatus(report.id, chosen.name());

                    Toast.makeText(getContext(),
                            "Statut mis à jour : " + chosen.name(),
                            Toast.LENGTH_SHORT).show();

                    dialog.dismiss();
                })
                .setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binder = null;
    }
}