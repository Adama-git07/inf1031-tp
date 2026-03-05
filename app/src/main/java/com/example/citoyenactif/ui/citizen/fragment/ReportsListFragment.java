package com.example.citoyenactif.ui.citizen.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.citoyenactif.databinding.RepportsListFragmentBinding;
import com.example.citoyenactif.ui.citizen.ReportAdapter;
import com.example.citoyenactif.ui.citizen.ReportViewModel;

public class ReportsListFragment extends Fragment {

    private RepportsListFragmentBinding binder;
    private ReportViewModel viewModel;
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

        // Initialisation ViewModel
        viewModel = new ViewModelProvider(this).get(ReportViewModel.class);

        // Initialisation RecyclerView
        adapter = new ReportAdapter(report -> {
            // TODO: ouvrir détails rapport
        });
        binder.rvReports.setLayoutManager(new LinearLayoutManager(getContext()));
        binder.rvReports.setAdapter(adapter);

        // Observer les rapports
        viewModel.getAllReports().observe(getViewLifecycleOwner(), reports -> {
            adapter.setReports(reports);
        });

        // Suppression par appui long gérée dans l'adapter
        adapter.setLongClickListener(report -> {
            viewModel.delete(report);
        });

        // Bouton ajout
        binder.fabAddReport.setOnClickListener(v -> {
            // TODO: ouvrir formulaire ajout rapport
        });
    }
}