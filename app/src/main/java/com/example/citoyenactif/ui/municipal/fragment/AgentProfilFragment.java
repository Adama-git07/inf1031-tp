package com.example.citoyenactif.ui.municipal.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.citoyenactif.databinding.AgentProfilFragmentBinding;
import com.example.citoyenactif.ui.viewmodel.AgentProfileViewModel;
import com.example.citoyenactif.util.SessionManager;

/**
 * Fragment Profil agent municipal.
 *
 * Architecture MVVM :
 *   - AgentProfileViewModel charge l'agent depuis Room en arrière-plan.
 *   - Le Fragment observe le LiveData et peuple les vues.
 *   - Aucun accès direct au repository ni création de Thread dans le Fragment.
 */
public class AgentProfilFragment extends Fragment {

    private AgentProfilFragmentBinding binder;

    /** ViewModel du profil agent */
    private AgentProfileViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        binder = AgentProfilFragmentBinding.inflate(inflater, container, false);

        // Obtenir le ViewModel
        viewModel = new ViewModelProvider(this).get(AgentProfileViewModel.class);

        // ── Observation des données agent ──────────────────────────────────────
        viewModel.getAgent().observe(getViewLifecycleOwner(), agent -> {
            if (agent == null) {
                Toast.makeText(getContext(), "Agent introuvable", Toast.LENGTH_SHORT).show();
                binder.tvAgentName.setText("Agent");
                binder.tvAgentNumber.setText("Numéro agent : —");
                return;
            }

            String fullName = safe(agent.name) + " " + safe(agent.firstName);
            binder.tvAgentName.setText(fullName.trim().isEmpty() ? "Agent municipal" : fullName.trim());
            binder.tvAgentNumber.setText("Numéro agent : " + safe(agent.agentNumber));
        });

        // Déclencher le chargement
        loadConnectedAgent();

        return binder.getRoot();
    }

    /**
     * Lance le chargement de l'agent connecté via le ViewModel.
     */
    private void loadConnectedAgent() {
        boolean isAgent = SessionManager.isAgent(requireContext());
        long agentId    = SessionManager.getUserId(requireContext());

        if (!isAgent || agentId <= 0) {
            Toast.makeText(getContext(), "Aucun agent connecté", Toast.LENGTH_SHORT).show();
            binder.tvAgentName.setText("Agent");
            binder.tvAgentNumber.setText("Numéro agent : —");
            return;
        }

        // Délégation au ViewModel — aucun Thread ici
        viewModel.loadAgent(agentId);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binder = null;
    }
}
