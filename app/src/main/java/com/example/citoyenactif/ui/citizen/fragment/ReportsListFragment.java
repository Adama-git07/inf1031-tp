package com.example.citoyenactif.ui.citizen.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.citoyenactif.data.local.entity.ReportEntity;
import com.example.citoyenactif.data.repository.ReportRepository;
import com.example.citoyenactif.databinding.ReportsListFragBinding;
import com.example.citoyenactif.ui.citizen.adapter.ReportAdapter;
import com.example.citoyenactif.ui.report.ReportActivity;

import java.util.List;

/**
 * Fragment affichant la liste des rapports (citoyen).
 * - Affiche les rapports sauvegardés en Room
 * - Bouton + ouvre ReportActivity
 * - Appui long : suppression du rapport
 */
public class ReportsListFragment extends Fragment {

    private ReportsListFragBinding binding;
    private ReportAdapter adapter;

    // Code request pour  rafraîchir après ajout
    private static final int REQ_ADD_REPORT = 1001;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = ReportsListFragBinding.inflate(inflater, container, false);

        setupRecycler();
        setupActions();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recharge la liste chaque fois qu'on revient sur ce fragment
        loadReports();
    }

    private void setupRecycler() {
        adapter = new ReportAdapter(
                report -> {
                    // Click simple : pour l'instant on peut afficher un toast
                    Toast.makeText(getContext(),
                            "Rapport: " + report.title,
                            Toast.LENGTH_SHORT).show();
                },
                this::confirmDelete
        );

        binding.rvReports.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvReports.setAdapter(adapter);
    }

    private void setupActions() {
        binding.fabAddReport.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ReportActivity.class);
            startActivity(intent); // on reload via onResume()
        });
    }

    /**
     * Charge les rapports depuis Room dans un thread secondaire.
     */
    private void loadReports() {
        new Thread(() -> {
            ReportRepository repo = new ReportRepository(requireContext());

            // Pour l’instant : tous les rapports
            // Plus tard : repo.getReportsByUser(userId) avec un SessionManager
            List<ReportEntity> reports = repo.getAllReports();

            requireActivity().runOnUiThread(() -> {
                adapter.setItems(reports);

                boolean empty = (reports == null || reports.isEmpty());
                binding.tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            });
        }).start();
    }

    /**
     * Demande confirmation avant suppression.
     */
    private void confirmDelete(ReportEntity report) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Supprimer le rapport")
                .setMessage("Voulez-vous supprimer ce rapport ?")
                .setPositiveButton("Supprimer", (dialog, which) -> deleteReport(report))
                .setNegativeButton("Annuler", null)
                .show();
    }

    /**
     * Supprime un rapport dans Room (thread secondaire) puis rafraîchit.
     */
    private void deleteReport(ReportEntity report) {
        new Thread(() -> {
            ReportRepository repo = new ReportRepository(requireContext());
            int deleted = repo.deleteReport(report);

            requireActivity().runOnUiThread(() -> {
                if (deleted > 0) {
                    Toast.makeText(getContext(), "Rapport supprimé", Toast.LENGTH_SHORT).show();
                    loadReports();
                } else {
                    Toast.makeText(getContext(), "Suppression échouée", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
}