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
 * Affiche toutes les informations de l'agent connecté :
 *   - Nom complet, rôle
 *   - Nom, prénom, matricule (numéro d'agent)
 *
 * Architecture MVVM :
 *   - AgentProfileViewModel charge l'agent depuis Room en arrière-plan.
 *   - Le Fragment observe le LiveData et peuple les vues.
 */
public class AgentProfilFragment extends Fragment {

    private AgentProfilFragmentBinding binder;
    private AgentProfileViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        binder = AgentProfilFragmentBinding.inflate(inflater, container, false);

        viewModel = new ViewModelProvider(this).get(AgentProfileViewModel.class);

        // ── Observation des données agent ──────────────────────────────────────
        viewModel.getAgent().observe(getViewLifecycleOwner(), agent -> {
            if (agent == null) {
                Toast.makeText(getContext(), "Agent introuvable", Toast.LENGTH_SHORT).show();
                return;
            }

            // Carte 1 — Nom complet + rôle
            String fullName = (safe(agent.firstName) + " " + safe(agent.name)).trim();
            binder.tvAgentFullName.setText(fullName.isEmpty() ? "Agent municipal" : fullName);
            binder.tvAgentRole.setText("Agent municipal");

            // Carte 2 — Détails
            binder.tvAgentName.setText(safe(agent.name, "—"));
            binder.tvAgentFirstName.setText(safe(agent.firstName, "—"));
            binder.tvAgentNumber.setText("Matricule : " + safe(agent.agentNumber, "—"));
        });

        loadConnectedAgent();

        return binder.getRoot();
    }

    private void loadConnectedAgent() {
        boolean isAgent = SessionManager.isAgent(requireContext());
        long agentId    = SessionManager.getUserId(requireContext());

        if (!isAgent || agentId <= 0) {
            Toast.makeText(getContext(), "Aucun agent connecté", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.loadAgent(agentId);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String safe(String s, String fallback) {
        return (s == null || s.isEmpty()) ? fallback : s;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binder = null;
    }
}
