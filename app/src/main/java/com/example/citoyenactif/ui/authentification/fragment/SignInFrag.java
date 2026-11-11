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
import com.example.citoyenactif.model.dto.SignInFormDTO;
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

    /** Formulaire lié au layout en double binding (@={dto.xxx}) : plus aucun getText() */
    private final SignInFormDTO dto = new SignInFormDTO();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        binder = SignInFragBinding.inflate(inflater, container, false);

        // Obtenir le ViewModel (survit aux rotations d'écran)
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Liaison du formulaire : saisie, choix Citoyen/Agent et visibilité des deux
        // formulaires sont gérés par le binding (voir sign_in_frag.xml)
        binder.setDto(dto);
        binder.setLifecycleOwner(getViewLifecycleOwner());

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
     * Détermine quel type de connexion effectuer selon le choix du formulaire (dto.agent).
     */
    private void handleLogin() {
        if (dto.agent.get()) {
            loginAgent();
        } else {
            loginCitizen();
        }
    }

    /**
     * Connexion citoyen : valide les champs du DTO et délègue au ViewModel.
     */
    private void loginCitizen() {
        String login    = clean(dto.username.get());
        String password = clean(dto.password.get());

        if (login.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Veuillez entrer vos identifiants", Toast.LENGTH_SHORT).show();
            return;
        }

        // Délégation au ViewModel — aucun Thread ici
        viewModel.loginCitizen(login, password);
    }

    /**
     * Connexion agent : valide les champs du DTO et délègue au ViewModel.
     */
    private void loginAgent() {
        String agentNumber = clean(dto.agentNumber.get());
        String password    = clean(dto.agentPassword.get());

        if (agentNumber.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Veuillez entrer vos identifiants agent", Toast.LENGTH_SHORT).show();
            return;
        }

        // Délégation au ViewModel — aucun Thread ici
        viewModel.loginAgent(agentNumber, password);
    }

    /** Valeur d'un champ sans espaces ; chaîne vide si null. */
    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binder = null;
    }
}