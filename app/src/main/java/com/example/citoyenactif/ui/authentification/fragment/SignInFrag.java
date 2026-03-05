package com.example.citoyenactif.ui.authentification.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.citoyenactif.data.local.entity.AgentEntity;
import com.example.citoyenactif.data.local.entity.UserEntity;
import com.example.citoyenactif.data.repository.AgentRepository;
import com.example.citoyenactif.data.repository.UserRepository;
import com.example.citoyenactif.databinding.SignInFragBinding;
import com.example.citoyenactif.ui.citizen.CitizenPageActivity;
import com.example.citoyenactif.ui.municipal.MunicipalPageActivity;
import com.example.citoyenactif.util.SessionManager;

public class SignInFrag extends Fragment {

    private SignInFragBinding binder;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        binder = SignInFragBinding.inflate(inflater, container, false);

        // Affichage initial : Citoyen
        showCitizenForm();

        // Switch Citoyen / Agent
        binder.rgAccountType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == binder.rbCitizen.getId()) {
                showCitizenForm();
            } else if (checkedId == binder.rbAgent.getId()) {
                showAgentForm();
            }
        });

        // Bouton login
        binder.btnLogin.setOnClickListener(v -> handleLogin());

        return binder.getRoot();
    }

    private void showCitizenForm() {
        // ✅ Version "propre" : on montre/cachent les 2 conteneurs
        binder.citizenForm.setVisibility(View.VISIBLE);
        binder.agentForm.setVisibility(View.GONE);

        // Optionnel : vider les champs agent
        binder.etAgentNumber.setText("");
        binder.etAgentPassword.setText("");
    }

    private void showAgentForm() {
        binder.citizenForm.setVisibility(View.GONE);
        binder.agentForm.setVisibility(View.VISIBLE);

        // Optionnel : vider les champs citizen
        binder.etUsername.setText("");
        binder.etPassword.setText("");
    }

    private void handleLogin() {
        boolean isAgent = binder.rgAccountType.getCheckedRadioButtonId() == binder.rbAgent.getId();
        if (isAgent) loginAgent();
        else loginCitizen();
    }

    private void loginCitizen() {
        final String login = binder.etUsername.getText().toString().trim();
        final String password = binder.etPassword.getText().toString().trim();

        if (login.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Veuillez entrer vos identifiants", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            UserRepository repo = new UserRepository(requireContext());
            UserEntity user = repo.login(login, password);

            requireActivity().runOnUiThread(() -> {
                if (user != null) {
                    SessionManager.saveCitizen(requireContext(), user.id);
                    startActivity(new Intent(requireActivity(), CitizenPageActivity.class));
                    requireActivity().finish();
                } else {
                    Toast.makeText(getContext(), "Login et/ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void loginAgent() {
        final String agentNumber = binder.etAgentNumber.getText().toString().trim();
        final String password = binder.etAgentPassword.getText().toString().trim();

        if (agentNumber.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Veuillez entrer vos identifiants agent", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            AgentRepository repo = new AgentRepository(requireContext());
            AgentEntity agent = repo.login(agentNumber, password);

            requireActivity().runOnUiThread(() -> {
                if (agent != null) {
                    SessionManager.saveAgent(requireContext(), agent.id);

                    startActivity(new Intent(requireActivity(), MunicipalPageActivity.class));
                    requireActivity().finish();
                } else {
                    Toast.makeText(getContext(), "Numéro d'agent et/ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binder = null; // bonne pratique fragments + databinding
    }
}