package com.example.citoyenactif.ui.authentification.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.citoyenactif.databinding.ResetPasswordFragBinding;
import com.example.citoyenactif.model.dto.ResetPasswordDTO;
import com.example.citoyenactif.ui.authentification.AuthentificationActivity;
import com.example.citoyenactif.ui.viewmodel.AuthViewModel;

/**
 * Fragment permettant de réinitialiser le mot de passe d'un utilisateur.
 *
 * L'utilisateur doit saisir :
 * - son nom d'utilisateur
 * - un nouveau mot de passe
 * - confirmer ce mot de passe
 *
 * Architecture MVVM :
 *   - AuthViewModel gère la mise à jour Room en arrière-plan.
 *   - Le Fragment observe le résultat et navigue / affiche un Toast.
 *   - Aucun accès direct au repository ni création de Thread dans le Fragment.
 */
public class ResetPasswordFrag extends Fragment {

    /** Binding permettant d'accéder aux vues du layout */
    private ResetPasswordFragBinding binder;

    /** ViewModel partagé pour toutes les opérations d'authentification */
    private AuthViewModel viewModel;

    /** Champs liés au layout en double binding (@={dto.xxx}) : plus aucun getText() */
    private final ResetPasswordDTO dto = new ResetPasswordDTO();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        binder = ResetPasswordFragBinding.inflate(inflater, container, false);

        // Liaison du DTO au layout
        binder.setDto(dto);
        binder.setLifecycleOwner(getViewLifecycleOwner());

        // Obtenir le ViewModel
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Bouton Annuler
        binder.cancelButton.setOnClickListener(v ->
                ((AuthentificationActivity) requireActivity()).goToSignIn()
        );

        // Bouton Confirmer
        binder.confirmButton.setOnClickListener(v -> resetPassword());

        // ── Observation du résultat ────────────────────────────────────────────
        viewModel.getResetPasswordResult().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(getContext(), "Mot de passe modifié avec succès", Toast.LENGTH_SHORT).show();
                ((AuthentificationActivity) requireActivity()).goToSignIn();
            } else if (success != null) {
                Toast.makeText(getContext(), "Utilisateur introuvable", Toast.LENGTH_SHORT).show();
            }
        });

        return binder.getRoot();
    }

    /**
     * Vérifie les champs et délègue la réinitialisation au ViewModel.
     */
    private void resetPassword() {
        String username        = clean(dto.username.get());
        String password        = clean(dto.password.get());
        String confirmPassword = clean(dto.confirmPassword.get());

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(getContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(getContext(), "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
            return;
        }

        // Délégation au ViewModel — aucun Thread ici
        viewModel.resetPassword(username, password);
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
