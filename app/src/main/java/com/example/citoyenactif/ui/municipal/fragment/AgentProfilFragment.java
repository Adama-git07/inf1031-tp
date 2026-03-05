package com.example.citoyenactif.ui.municipal.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.citoyenactif.data.local.entity.AgentEntity;
import com.example.citoyenactif.data.repository.AgentRepository;
import com.example.citoyenactif.databinding.AgentProfilFragmentBinding;
import com.example.citoyenactif.util.SessionManager;

public class AgentProfilFragment extends Fragment {

    private AgentProfilFragmentBinding binder;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        binder = AgentProfilFragmentBinding.inflate(inflater, container, false);

        loadConnectedAgent();

        return binder.getRoot();
    }

    private void loadConnectedAgent() {
        boolean isAgent = SessionManager.isAgent(requireContext());
        long agentId = SessionManager.getUserId(requireContext()); // on stocke l'id agent ici

        if (!isAgent || agentId <= 0) {
            Toast.makeText(getContext(), "Aucun agent connecté", Toast.LENGTH_SHORT).show();
            binder.tvAgentName.setText("Agent");
            binder.tvAgentNumber.setText("Numéro agent : —");
            return;
        }

        new Thread(() -> {
            AgentRepository repo = new AgentRepository(requireContext());
            AgentEntity agent = repo.getById(agentId);

            requireActivity().runOnUiThread(() -> {
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

        }).start();
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