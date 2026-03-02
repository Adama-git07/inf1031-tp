package com.example.citoyenactif.authentification.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.citoyenactif.citizen.CitizenPageActivity;
import com.example.citoyenactif.databinding.SignInFragBinding;

/**
 * Fragment de connexion (SignIn).
 *
 * Ce fragment permet à l'utilisateur :
 * - De saisir son nom d'utilisateur
 * - De saisir son mot de passe
 * - De se connecter à l'application
 *
 * Si les champs sont valides, l'utilisateur est redirigé
 * vers CitizenPageActivity.
 */
public class SignInFrag extends Fragment {

    /**
     * Binding généré automatiquement par DataBinding
     * permettant d'accéder aux vues du layout sign_in_frag.xml.
     */
    private SignInFragBinding binder;

    /**
     * Méthode appelée lors de la création de la vue du fragment.
     *
     * @param inflater LayoutInflater permettant d'inflater le layout XML
     * @param container ViewGroup parent dans lequel le fragment est inséré
     * @param saveInstanceState Bundle contenant l'état sauvegardé (si existant)
     * @return View racine du fragment
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle saveInstanceState) {

        // Initialisation du binding (liaison XML → Java)
        binder = SignInFragBinding.inflate(inflater, container, false);

        /**
         * Gestion du clic sur le bouton "Connexion".
         * - Vérifie que les champs ne sont pas vides
         * - Lance l'activité principale CitizenPageActivity
         * - Ferme l'activité d'authentification pour empêcher
         *   le retour via le bouton "Back"
         */
        binder.btnLogin.setOnClickListener(v -> {

            String username = binder.etUsername.getText().toString().trim();
            String password = binder.etPassword.getText().toString().trim();

            // Vérification simple des champs
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(),
                        "Veuillez entrer vos identifiants",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Lancer CitizenPageActivity
            Intent intent = new Intent(requireActivity(), CitizenPageActivity.class);
            startActivity(intent);

            // Fermer AuthentificationActivity pour éviter retour au login
            requireActivity().finish();
        });

        // Retourne la vue racine du fragment
        return binder.getRoot();
    }
}