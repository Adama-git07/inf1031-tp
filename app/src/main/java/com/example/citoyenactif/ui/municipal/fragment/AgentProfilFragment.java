package com.example.citoyenactif.ui.municipal.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.citoyenactif.databinding.AgentProfilFragmentBinding;

public class AgentProfilFragment extends Fragment {

    private AgentProfilFragmentBinding binder;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        binder = AgentProfilFragmentBinding.inflate(inflater, container, false);

        // TODO: afficher les infos réelles de l’agent connecté via SessionManager + Room
        binder.tvAgentName.setText("Agent municipal");
        binder.tvAgentNumber.setText("Numéro agent : —");

        return binder.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binder = null;
    }
}