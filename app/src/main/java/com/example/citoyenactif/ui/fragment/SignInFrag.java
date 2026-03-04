package com.example.citoyenactif.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.citoyenactif.citizen.CitizenPageActivity;
import com.example.citoyenactif.data.local.entity.UserEntity;
import com.example.citoyenactif.data.repository.UserRepository;
import com.example.citoyenactif.databinding.SignInFragBinding;

/**
 * Fragment de connexion (SignIn).
 *
 * Ce fragment permet à l'utilisateur :
 * - De saisir son nom d'utilisateur (ou email)
 * - De saisir son mot de passe
 * - De se connecter à l'application
 *
 * La vérification se fait via Room (UserRepository -> UserDao).
 * Si les identifiants sont valides, l'utilisateur est redirigé vers CitizenPageActivity.
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
     * @param savedInstanceState Bundle contenant l'état sauvegardé (si existant)
     * @return View racine du fragment
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        // Initialisation du binding (liaison XML → Java)
        binder = SignInFragBinding.inflate(inflater, container, false);

        /**
         * Gestion du clic sur le bouton "Connexion".
         * - Vérifie que les champs ne sont pas vides
         * - Lance la vérification des identifiants dans Room (thread secondaire)
         * - En cas de succès : ouvre CitizenPageActivity et ferme l'activité d'authentification
         * - En cas d'échec : affiche "Login ou mot de passe incorrect"
         */
        binder.btnLogin.setOnClickListener(v -> handleLogin());

        // Retourne la vue racine du fragment
        return binder.getRoot();
    }

    /**
     * Vérifie les identifiants de l'utilisateur via Room.
     *
     * Utilise un thread secondaire car les accès Room ne doivent
     * pas être exécutés sur le thread principal (UI).
     */
    private void handleLogin() {

        // Récupération des champs (login = username OU email)
        final String login = binder.etUsername.getText().toString().trim();
        final String password = binder.etPassword.getText().toString().trim();

        // Vérification simple des champs
        if (login.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(),
                    "Veuillez entrer vos identifiants",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Accès base de données dans un thread secondaire
        new Thread(() -> {
            UserRepository repo = new UserRepository(requireContext());

            // Vérifie dans Room si un user correspond (username/email + password)
            UserEntity user = repo.login(login, password);

            // Retour sur le thread UI pour afficher / naviguer
            requireActivity().runOnUiThread(() -> {
                if (user != null) {
                    // Identifiants corrects -> ouvrir la page citoyen
                    Intent intent = new Intent(requireActivity(), CitizenPageActivity.class);
                    startActivity(intent);

                    // Fermer AuthentificationActivity pour éviter retour au login
                    requireActivity().finish();
                } else {
                    // Identifiants incorrects
                    Toast.makeText(getContext(),
                            "Login et/ou mot de passe incorrect",
                            Toast.LENGTH_SHORT).show();
                }
            });

        }).start();
    }
}