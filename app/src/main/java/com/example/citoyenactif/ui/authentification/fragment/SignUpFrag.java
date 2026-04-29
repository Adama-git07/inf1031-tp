package com.example.citoyenactif.ui.authentification.fragment;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.citoyenactif.databinding.SignUpFragBinding;
import com.example.citoyenactif.model.dto.UserCreateDTO;
import com.example.citoyenactif.model.enums.UserRoles;
import com.example.citoyenactif.ui.authentification.AuthentificationActivity;
import com.example.citoyenactif.ui.viewmodel.AuthViewModel;

/**
 * Fragment de création de compte citoyen (SignUp).
 *
 * Utilise DataBinding two-way :
 *   - Le layout sign_up_frag.xml déclare <variable name="dto" type="UserCreateDTO"/>
 *   - Chaque EditText est lié @={dto.field} → saisie auto-écrite dans le DTO
 *   - La validation lit dto.field.get() sans aucun getText()
 *
 * Architecture MVVM :
 *   - AuthViewModel gère l'inscription en arrière-plan.
 *   - Le Fragment observe le résultat et affiche un Toast / navigue.
 *   - Aucun accès direct au repository ni création de Thread dans le Fragment.
 *
 * Validations effectuées avant l'inscription :
 *   1. Nom et prénom non vides
 *   2. Format email valide (Patterns.EMAIL_ADDRESS)
 *   3. Téléphone : exactement 10 chiffres
 *   4. Username non vide
 *   5. Mot de passe ≥ 6 caractères
 *   6. Confirmation identique au mot de passe
 */
public class SignUpFrag extends Fragment {

    private SignUpFragBinding binder;

    /** DTO lié au layout via DataBinding — rempli automatiquement par la saisie */
    private final UserCreateDTO dto = new UserCreateDTO();

    /** ViewModel partagé pour toutes les opérations d'authentification */
    private AuthViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        binder = SignUpFragBinding.inflate(inflater, container, false);

        // Liaison du DTO au layout
        binder.setDto(dto);
        binder.setLifecycleOwner(getViewLifecycleOwner());

        // Obtenir le ViewModel
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Bouton Annuler
        binder.cancelButton.setOnClickListener(v ->
                ((AuthentificationActivity) requireActivity()).goToSignIn()
        );

        // Bouton Confirmer → validation puis inscription
        binder.confirmButton.setOnClickListener(v -> handleSignUp());

        // ── Observation des résultats ──────────────────────────────────────────

        // Résultat de l'inscription
        viewModel.getRegisterResult().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                toast("Compte créé avec succès !");
                ((AuthentificationActivity) requireActivity()).goToSignIn();
            }
            // Le cas false est géré par errorMessage
        });

        // Message d'erreur (ex. doublon email/username)
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                toast(msg);
            }
        });

        return binder.getRoot();
    }

    /**
     * Lit les champs du DTO (mis à jour automatiquement par DataBinding),
     * valide les données, puis délègue l'inscription au ViewModel.
     */
    private void handleSignUp() {

        // ── Lecture des champs via ObservableField.get() ──────────────────────
        String lastName   = get(dto.lastName);
        String firstName  = get(dto.firstName);
        String address    = get(dto.address);
        String email      = get(dto.email);
        String phone      = get(dto.phone);
        String username   = get(dto.username);
        String password   = get(dto.password);
        String confirmPwd = get(dto.confirmPassword);

        // ── Validations ───────────────────────────────────────────────────────

        if (lastName.isEmpty()) {
            toast("Veuillez saisir votre nom"); return;
        }
        if (firstName.isEmpty()) {
            toast("Veuillez saisir votre prénom"); return;
        }
        if (address.isEmpty()) {
            toast("Veuillez saisir votre adresse"); return;
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            toast("Adresse courriel invalide"); return;
        }
        if (!phone.matches("[0-9]{10}")) {
            toast("Numéro de téléphone invalide (10 chiffres requis)"); return;
        }
        if (username.isEmpty()) {
            toast("Veuillez choisir un nom d'utilisateur"); return;
        }
        if (password.length() < 6) {
            toast("Le mot de passe doit contenir au moins 6 caractères"); return;
        }
        if (!password.equals(confirmPwd)) {
            toast("Les mots de passe ne correspondent pas"); return;
        }

        // ── Inscription via le ViewModel ──────────────────────────────────────
        dto.role = UserRoles.CITIZEN;
        viewModel.register(dto);
    }

    /** Lit un ObservableField<String> en null-safe et retourne une chaîne trimmée. */
    private String get(androidx.databinding.ObservableField<String> field) {
        String v = field.get();
        return v != null ? v.trim() : "";
    }

    private void toast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binder = null;
    }
}
