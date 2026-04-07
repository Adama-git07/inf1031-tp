package com.example.citoyenactif.ui.citizen.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.citoyenactif.databinding.RepportsListFragmentBinding;
import com.example.citoyenactif.ui.citizen.adapter.ReportAdapter;
import com.example.citoyenactif.ui.report.ReportActivity;

/**
 * Fragment affichant la liste des rapports du citoyen connecté.
 *
 * Fix #3 : la description est maintenant affichée dans chaque carte.
 * Fix #6 : clic → ouvre le rapport en mode VISUALISATION (ReportActivity).
 * Fix #6 : appui long → boîte de dialogue de confirmation avant suppression.
 */
public class ReportsListFragment extends Fragment {

    private RepportsListFragmentBinding binder;
    private ReportActivity.ReportViewModel viewModel;
    private ReportAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binder = RepportsListFragmentBinding.inflate(inflater, container, false);
        return binder.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ReportActivity.ReportViewModel.class);

        adapter = new ReportAdapter(

                // ── Clic simple → ouvrir en mode VISUALISATION ──────────────────
                report -> {
                    Intent intent = new Intent(requireContext(), ReportActivity.class);
                    intent.putExtra(ReportActivity.EXTRA_REPORT_ID, report.id);
                    startActivity(intent);
                },

                // ── Appui long → confirmation avant suppression (Fix #6) ─────────
                report -> {
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Supprimer le rapport")
                            .setMessage("Voulez-vous vraiment supprimer « " + report.title + " » ?\nCette action est irréversible.")
                            .setPositiveButton("Supprimer", (dialog, which) -> viewModel.delete(report))
                            .setNegativeButton("Annuler", null)
                            .show();
                }
        );

        binder.rvReports.setLayoutManager(new LinearLayoutManager(getContext()));
        binder.rvReports.setAdapter(adapter);

        viewModel.getAllReports().observe(getViewLifecycleOwner(), reports ->
                adapter.setItems(reports)
        );

        // FAB → créer un nouveau rapport
        binder.fabAddReport.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ReportActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binder = null;
    }
}
