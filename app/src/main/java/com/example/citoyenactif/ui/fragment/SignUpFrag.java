package com.example.citoyenactif.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.citoyenactif.data.repository.UserRepository;
import com.example.citoyenactif.model.dto.UserCreateDTO;
import com.example.citoyenactif.model.enums.UserRoles;
import com.example.citoyenactif.ui.AuthentificationActivity;
import com.example.citoyenactif.databinding.SignUpFragBinding;

/**
 * Fragment de création de compte (SignUp).
 *
 * Ce fragment permet à un utilisateur
 * de créer un nouveau compte dans l'application.
 *
 * Il récupère les informations saisies,
 * puis appelle le Repository pour enregistrer
 * l'utilisateur dans la base de données Room.
 */
public class SignUpFrag extends Fragment {

    /**
     * Binding généré automatiquement pour accéder
     * aux vues du layout sign_up_frag.xml.
     */
    private SignUpFragBinding binder;

    /**
     * Création et initialisation de la vue du fragment.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        // Liaison entre le XML et le code Java
        binder = SignUpFragBinding.inflate(inflater, container, false);

        /**
         * Bouton Annuler :
         * Retourne vers l'écran de connexion (SignIn).
         */
        binder.cancelButton.setOnClickListener(v ->
                ((AuthentificationActivity) requireActivity()).goToSignIn()
        );

        /**
         * Bouton Confirmer :
         * Lance le processus de création du compte.
         */
        binder.confirmButton.setOnClickListener(v -> {

            // Room ne peut pas être appelé sur le thread principal
            new Thread(() -> {

                try {
                    // Création du repository
                    UserRepository repo = new UserRepository(requireContext());

                    // Création du DTO à partir des champs saisis
                    UserCreateDTO dto = new UserCreateDTO();
                    dto.username = binder.username.getText().toString().trim();
                    dto.email = binder.email.getText().toString().trim();
                    dto.password = binder.password.getText().toString().trim();

                    // Attribution du rôle par défaut (CITIZEN)
                    dto.role = UserRoles.valueOf(UserRoles.CITIZEN.name());

                    // Enregistrement en base
                    long id = repo.register(dto);

                    // Retour au thread UI pour afficher un message
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(),
                                    "Compte créé (id=" + id + ")",
                                    Toast.LENGTH_SHORT).show()
                    );

                } catch (Exception e) {

                    // Gestion d'erreur (ex : username/email déjà utilisé)
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(),
                                    "Erreur : username/email déjà utilisé",
                                    Toast.LENGTH_SHORT).show()
                    );
                }

            }).start();
        });

        // Retourne la vue racine du fragment
        return binder.getRoot();
    }
}