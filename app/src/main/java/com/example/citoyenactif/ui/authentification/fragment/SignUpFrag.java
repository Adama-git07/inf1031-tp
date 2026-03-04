package com.example.citoyenactif.ui.authentification.fragment;

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
import com.example.citoyenactif.ui.authentification.AuthentificationActivity;
import com.example.citoyenactif.databinding.SignUpFragBinding;

/**
 * Fragment de création de compte (SignUp).
 *
 * Crée uniquement des comptes citoyens (UserRoles.CITIZEN),
 * puis redirige automatiquement vers le fragment de connexion.
 */
public class SignUpFrag extends Fragment {

    private SignUpFragBinding binder;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        binder = SignUpFragBinding.inflate(inflater, container, false);

        // Annuler -> retour à la connexion
        binder.cancelButton.setOnClickListener(v ->
                ((AuthentificationActivity) requireActivity()).goToSignIn()
        );

        // Confirmer -> création compte
        binder.confirmButton.setOnClickListener(v -> createAccount());

        return binder.getRoot();
    }

    /**
     * Crée un compte citoyen dans Room dans un thread secondaire.
     */
    private void createAccount() {

        new Thread(() -> {
            try {
                UserRepository repo = new UserRepository(requireContext());

                UserCreateDTO dto = new UserCreateDTO();
                dto.username = binder.username.getText().toString().trim();
                dto.email = binder.email.getText().toString().trim();
                dto.password = binder.password.getText().toString().trim();

                //SignUp = citoyen uniquement
                dto.role = UserRoles.CITIZEN;

                long id = repo.register(dto);

                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(),
                            "Compte créé (id=" + id + "). Connectez-vous.",
                            Toast.LENGTH_SHORT).show();

                    //retour automatique au fragment de connexion
                    ((AuthentificationActivity) requireActivity()).goToSignIn();
                });

            } catch (Exception e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(),
                                "Erreur : username/email déjà utilisé",
                                Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
}