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
 * Utilise DataBinding :
 *   - Le layout agent_profil_fragment.xml déclare <variable name="agent"/>
 *   - Les textes (nom, prénom, matricule) se remplissent avec @{agent.xxx}
 *   - Le Fragment n'appelle plus setText() sur chaque vue
 *
 * Architecture MVVM :
 *   - AgentProfileViewModel charge l'agent depuis Room en arrière-plan.
 *   - Le Fragment observe le LiveData et donne l'agent au binding.
 */
public class AgentProfilFragment extends Fragment {

    private AgentProfilFragmentBinding binder;
    private AgentProfileViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        binder = AgentProfilFragmentBinding.inflate(inflater, container, false);
        binder.setLifecycleOwner(getViewLifecycleOwner());

        viewModel = new ViewModelProvider(this).get(AgentProfileViewModel.class);

        // Quand l'agent est chargé, on le donne au layout : les textes se mettent à jour seuls
        viewModel.getAgent().observe(getViewLifecycleOwner(), agent -> {
            if (agent == null) {
                Toast.makeText(getContext(), "Agent introuvable", Toast.LENGTH_SHORT).show();
                return;
            }
            binder.setAgent(agent);
        });

        loadConnectedAgent();

        return binder.getRoot();
    }

    /** Demande au ViewModel de charger l'agent connecté. */
    private void loadConnectedAgent() {
        boolean isAgent = SessionManager.isAgent(requireContext());
        long agentId    = SessionManager.getUserId(requireContext());

        if (!isAgent || agentId <= 0) {
            Toast.makeText(getContext(), "Aucun agent connecté", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.loadAgent(agentId);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binder = null;
    }
}