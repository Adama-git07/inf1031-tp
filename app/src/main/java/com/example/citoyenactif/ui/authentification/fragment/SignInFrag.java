package com.example.citoyenactif.ui.authentification.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.citoyenactif.databinding.SignInFragBinding;
import com.example.citoyenactif.ui.authentification.AuthentificationActivity;
import com.example.citoyenactif.ui.authentification.NfcScanActivity;
import com.example.citoyenactif.ui.citizen.CitizenPageActivity;
import com.example.citoyenactif.ui.municipal.MunicipalPageActivity;
import com.example.citoyenactif.ui.viewmodel.AuthViewModel;
import com.example.citoyenactif.util.SessionManager;
import com.example.citoyenactif.ui.authentification.NfcWriteActivity;

/**
 * Fragment de connexion (SignIn).
 *
 * Permet de se connecter soit en tant que :
 * - Citoyen (UserEntity) : login + mot de passe
 * - Agent municipal (AgentEntity) : numéro + mot de passe, OU carte NFC
 *
 * Architecture MVVM :
 * - AuthViewModel gère toutes les opérations Room en arrière-plan.
 * - Le Fragment observe les LiveData et met à jour l'UI en conséquence.
 * - Aucun accès direct aux repositories ni création de Thread dans le Fragment.
 */
public class SignInFrag extends Fragment {

    /** Binding DataBinding pour accéder aux vues de sign_in_frag.xml */
    private SignInFragBinding binder;

    /** ViewModel partagé pour toutes les opérations d'authentification */
    private AuthViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        binder = SignInFragBinding.inflate(inflater, container, false);

        // Obtenir le ViewModel (survit aux rotations d'écran)
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Écran par défaut : formulaire citoyen visible
        showCitizenForm();

        // Écoute du changement Citoyen / Agent
        binder.rgAccountType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == binder.rbCitizen.getId()) {
                showCitizenForm();
            } else if (checkedId == binder.rbAgent.getId()) {
                showAgentForm();
            }
        });

        // Clic sur bouton "Connexion"
        binder.btnLogin.setOnClickListener(v -> handleLogin());

        // Bouton "Activer avec ma carte NFC" -> écran de scan
        binder.btnNfc.setOnClickListener(v ->
                startActivity(new Intent(requireActivity(), NfcScanActivity.class)));

        // Lien "Programmer une carte d'agent" -> écran d'écriture
        binder.tvWriteCard.setOnClickListener(v ->
                startActivity(new Intent(requireActivity(), NfcWriteActivity.class)));

        // Lien "Créer un compte" → SignUpFrag
        binder.tvCreateAccount.setOnClickListener(v -> {
            if (requireActivity() instanceof AuthentificationActivity) {
                ((AuthentificationActivity) requireActivity()).goToSignUp();
            }
        });

        // Lien "Mot de passe oublié ?" → ResetPasswordFrag
        binder.tvForgotPassword.setOnClickListener(v -> {
            if (requireActivity() instanceof AuthentificationActivity) {
                ((AuthentificationActivity) requireActivity()).goToResetPassword();
            }
        });

        // ── Observation des résultats ──────────────────────────────────────────

        // Résultat connexion citoyen
        viewModel.getCitizenLoginResult().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                SessionManager.saveCitizen(requireContext(), user.id);
                startActivity(new Intent(requireActivity(), CitizenPageActivity.class));
                requireActivity().finish();
            } else {
                Toast.makeText(getContext(), "Login et/ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
            }
        });

        // Résultat connexion agent
        viewModel.getAgentLoginResult().observe(getViewLifecycleOwner(), agent -> {
            if (agent != null) {
                SessionManager.saveAgent(requireContext(), agent.id);
                startActivity(new Intent(requireActivity(), MunicipalPageActivity.class));
                requireActivity().finish();
            } else {
                Toast.makeText(getContext(), "Numéro d'agent et/ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
            }
        });

        return binder.getRoot();
    }

    /**
     * Affiche le formulaire citoyen et masque celui de l'agent.
     */
    private void showCitizenForm() {
        binder.citizenForm.setVisibility(View.VISIBLE);
        binder.agentForm.setVisibility(View.GONE);
        binder.etAgentNumber.setText("");
        binder.etAgentPassword.setText("");
    }

    /**
     * Affiche le formulaire agent et masque celui du citoyen.
     */
    private void showAgentForm() {
        binder.citizenForm.setVisibility(View.GONE);
        binder.agentForm.setVisibility(View.VISIBLE);
        binder.etUsername.setText("");
        binder.etPassword.setText("");
    }

    /**
     * Détermine quel type de connexion effectuer selon le RadioButton sélectionné.
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
     * Connexion citoyen : valide les champs et délègue au ViewModel.
     */
    private void loginCitizen() {
        String login    = binder.etUsername.getText().toString().trim();
        String password = binder.etPassword.getText().toString().trim();

        if (login.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Veuillez entrer vos identifiants", Toast.LENGTH_SHORT).show();
            return;
        }

        // Délégation au ViewModel — aucun Thread ici
        viewModel.loginCitizen(login, password);
    }

    /**
     * Connexion agent : valide les champs et délègue au ViewModel.
     */
    private void loginAgent() {
        String agentNumber = binder.etAgentNumber.getText().toString().trim();
        String password    = binder.etAgentPassword.getText().toString().trim();

        if (agentNumber.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Veuillez entrer vos identifiants agent", Toast.LENGTH_SHORT).show();
            return;
        }

        // Délégation au ViewModel — aucun Thread ici
        viewModel.loginAgent(agentNumber, password);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binder = null;
    }
}