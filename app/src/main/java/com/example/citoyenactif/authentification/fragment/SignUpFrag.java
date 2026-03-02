package com.example.citoyenactif.authentification.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.citoyenactif.authentification.AuthentificationActivity;
import com.example.citoyenactif.databinding.SignUpFragBinding;

/**
 * Fragment de création de compte (SignUp).
 *
 * Ce fragment permet à un nouvel utilisateur
 * de créer un compte dans l'application.
 *
 * Pour le moment, il contient uniquement l'inflation
 * du layout associé.
 */
public class SignUpFrag extends Fragment {

    /**
     * Binding généré automatiquement par DataBinding
     * pour accéder aux vues du layout sign_up_frag.xml.
     */
    private SignUpFragBinding binder;

    /**
     * Méthode appelée lors de la création de la vue du fragment.
     *
     * @param inflater permet d'inflater le layout XML
     * @param container conteneur parent du fragment
     * @param savedInstanceState état sauvegardé (si existant)
     * @return vue racine du fragment
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        // Initialisation du binding (liaison XML ↔ Java)
        binder = SignUpFragBinding.inflate(inflater, container, false);

        // Retourne à la page d'accueil
        binder.cancelButton.setOnClickListener(v -> {
            ((AuthentificationActivity) requireActivity()).goToSignIn();
        });

        // Retourne la vue racine du fragment
        return binder.getRoot();
    }
}