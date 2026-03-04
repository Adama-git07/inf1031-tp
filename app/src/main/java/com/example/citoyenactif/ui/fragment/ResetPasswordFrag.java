package com.example.citoyenactif.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.citoyenactif.data.repository.UserRepository;
import com.example.citoyenactif.databinding.ResetPasswordFragBinding;
import com.example.citoyenactif.ui.AuthentificationActivity;

/**
 * Fragment permettant de réinitialiser le mot de passe d'un utilisateur.
 *
 * L'utilisateur doit saisir :
 * - son nom d'utilisateur
 * - un nouveau mot de passe
 * - confirmer ce mot de passe
 *
 * Le système vérifie que les deux mots de passe sont identiques
 * avant de mettre à jour la base de données via Room.
 */
public class ResetPasswordFrag extends Fragment {

    /** Binding permettant d'accéder aux vues du layout */
    private ResetPasswordFragBinding binder;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        // Initialisation du binding
        binder = ResetPasswordFragBinding.inflate(inflater, container, false);

        /**
         * Bouton Annuler
         * Retourne au fragment de connexion
         */
        binder.cancelButton.setOnClickListener(v ->
                ((AuthentificationActivity) requireActivity()).goToSignIn()
        );

        /**
         * Bouton Confirmer
         * Lance la procédure de réinitialisation du mot de passe
         */
        binder.confirmButton.setOnClickListener(v -> resetPassword());

        return binder.getRoot();
    }

    /**
     * Vérifie les champs et met à jour le mot de passe dans Room.
     */
    private void resetPassword() {

        // Récupération des champs
        String username = binder.username.getText().toString().trim();
        String password = binder.password.getText().toString().trim();
        String confirmPassword = binder.confirmPassword.getText().toString().trim();

        // Vérification que les champs ne sont pas vides
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(getContext(),
                    "Veuillez remplir tous les champs",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Vérification que les mots de passe sont identiques
        if (!password.equals(confirmPassword)) {
            Toast.makeText(getContext(),
                    "Les mots de passe ne correspondent pas",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Room ne doit pas être appelé sur le thread principal
        new Thread(() -> {

            UserRepository repo = new UserRepository(requireContext());

            boolean success = repo.resetPasswordByUsername(username, password);

            requireActivity().runOnUiThread(() -> {

                if (success) {

                    Toast.makeText(getContext(),
                            "Mot de passe modifié avec succès",
                            Toast.LENGTH_SHORT).show();

                    // Retour à la page de connexion
                    ((AuthentificationActivity) requireActivity()).goToSignIn();

                } else {

                    Toast.makeText(getContext(),
                            "Utilisateur introuvable",
                            Toast.LENGTH_SHORT).show();
                }

            });

        }).start();
    }
}