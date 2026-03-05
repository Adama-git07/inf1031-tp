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

/**
 * Fragment de connexion (SignIn).
 *
 * Permet de se connecter soit en tant que :
 * - Citoyen (UserEntity)
 * - Agent municipal (AgentEntity)
 *
 * Comportement :
 * - Affiche un formulaire différent selon le RadioButton sélectionné
 * - Vérifie les champs
 * - Exécute la requête Room en thread secondaire (pour ne pas bloquer l'UI)
 * - En cas de succès : sauvegarde la session puis navigue vers l'activité correspondante
 */
public class SignInFrag extends Fragment {

    /**
     * Binding DataBinding pour accéder aux vues de sign_in_frag.xml
     * (ex: btnLogin, etUsername, citizenForm, agentForm, etc.)
     */
    private SignInFragBinding binder;

    /**
     * Création de la vue du fragment + initialisation UI.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        // Liaison XML -> Java
        binder = SignInFragBinding.inflate(inflater, container, false);

        // Écran par défaut : formulaire citoyen visible
        showCitizenForm();

        // Écoute du changement Citoyen / Agent
        binder.rgAccountType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == binder.rbCitizen.getId()) {
                // Si Citoyen sélectionné => on affiche le formulaire citoyen
                showCitizenForm();
            } else if (checkedId == binder.rbAgent.getId()) {
                // Si Agent sélectionné => on affiche le formulaire agent
                showAgentForm();
            }
        });

        // Clic sur bouton "Connexion" => déclenche le login selon le type sélectionné
        binder.btnLogin.setOnClickListener(v -> handleLogin());

        // Retourne la racine du layout
        return binder.getRoot();
    }

    /**
     * Affiche le formulaire citoyen et masque celui de l'agent.
     * Optionnel : vide les champs agent pour éviter confusion.
     */
    private void showCitizenForm() {
        binder.citizenForm.setVisibility(View.VISIBLE);
        binder.agentForm.setVisibility(View.GONE);

        // Nettoyage des champs agent
        binder.etAgentNumber.setText("");
        binder.etAgentPassword.setText("");
    }

    /**
     * Affiche le formulaire agent et masque celui du citoyen.
     * Optionnel : vide les champs citoyen pour éviter confusion.
     */
    private void showAgentForm() {
        binder.citizenForm.setVisibility(View.GONE);
        binder.agentForm.setVisibility(View.VISIBLE);

        // Nettoyage des champs citoyen
        binder.etUsername.setText("");
        binder.etPassword.setText("");
    }

    /**
     * Détermine quel type de connexion effectuer (agent ou citoyen)
     * selon le RadioButton sélectionné.
     */
    private void handleLogin() {
        boolean isAgent = binder.rgAccountType.getCheckedRadioButtonId() == binder.rbAgent.getId();
        if (isAgent) {
            loginAgent();
        } else {
            loginCitizen();
        }
    }

    /**
     * Connexion citoyen :
     * - Récupère login (username ou email) + password
     * - Vérifie que ce n'est pas vide
     * - Appelle UserRepository.login() en thread secondaire
     * - Si succès : sauvegarde userId dans SessionManager + ouvre CitizenPageActivity
     */
    private void loginCitizen() {
        final String login = binder.etUsername.getText().toString().trim();
        final String password = binder.etPassword.getText().toString().trim();

        // Vérification des champs
        if (login.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Veuillez entrer vos identifiants", Toast.LENGTH_SHORT).show();
            return;
        }

        // Thread secondaire pour Room
        new Thread(() -> {
            UserRepository repo = new UserRepository(requireContext());
            UserEntity user = repo.login(login, password);

            // Retour sur UI
            requireActivity().runOnUiThread(() -> {
                if (user != null) {
                    // Sauvegarde de la session (user connecté)
                    SessionManager.saveCitizen(requireContext(), user.id);

                    // Navigation vers l'écran citoyen
                    startActivity(new Intent(requireActivity(), CitizenPageActivity.class));

                    // Ferme l'activité d'authentification (empêche retour au login)
                    requireActivity().finish();
                } else {
                    Toast.makeText(getContext(), "Login et/ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    /**
     * Connexion agent :
     * - Récupère agentNumber + password
     * - Vérifie que ce n'est pas vide
     * - Appelle AgentRepository.login() en thread secondaire
     * - Si succès : sauvegarde agentId dans SessionManager + ouvre MunicipalPageActivity
     */
    private void loginAgent() {
        final String agentNumber = binder.etAgentNumber.getText().toString().trim();
        final String password = binder.etAgentPassword.getText().toString().trim();

        // Vérification des champs
        if (agentNumber.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Veuillez entrer vos identifiants agent", Toast.LENGTH_SHORT).show();
            return;
        }

        // Thread secondaire pour Room
        new Thread(() -> {
            AgentRepository repo = new AgentRepository(requireContext());
            AgentEntity agent = repo.login(agentNumber, password);

            // Retour sur UI
            requireActivity().runOnUiThread(() -> {
                if (agent != null) {
                    // Sauvegarde de la session (agent connecté)
                    SessionManager.saveAgent(requireContext(), agent.id);

                    // Navigation vers l'écran municipal
                    startActivity(new Intent(requireActivity(), MunicipalPageActivity.class));

                    // Ferme l'activité d'authentification
                    requireActivity().finish();
                } else {
                    Toast.makeText(getContext(), "Numéro d'agent et/ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    /**
     * Appelé quand la vue du fragment est détruite.
     * Bonne pratique : mettre binder à null pour éviter les fuites mémoire.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binder = null;
    }
}