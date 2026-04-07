package com.example.citoyenactif.ui.authentification.fragment;

import android.os.Bundle;
import android.util.Patterns;
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
 * Fragment de création de compte citoyen (SignUp).
 *
 * Utilise DataBinding two-way :
 *   - Le layout sign_up_frag.xml déclare <variable name="dto" type="UserCreateDTO"/>
 *   - Chaque EditText est lié @={dto.field} → saisie auto-écrite dans le DTO
 *   - La validation lit dto.field.get() sans aucun getText()
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        binder = SignUpFragBinding.inflate(inflater, container, false);

        // Liaison du DTO au layout — les champs @={dto.xxx} pointent vers ce DTO
        binder.setDto(dto);
        binder.setLifecycleOwner(getViewLifecycleOwner());

        // Bouton Annuler
        binder.cancelButton.setOnClickListener(v ->
                ((AuthentificationActivity) requireActivity()).goToSignIn()
        );

        // Bouton Confirmer → validation puis inscription
        binder.confirmButton.setOnClickListener(v -> handleSignUp());

        return binder.getRoot();
    }

    /**
     * Lit les champs du DTO (mis à jour automatiquement par DataBinding),
     * valide les données, puis inscrit l'utilisateur en base.
     */
    private void handleSignUp() {

        // ── Lecture des champs via ObservableField.get() ──────────────────────
        String lastName     = get(dto.lastName);
        String firstName    = get(dto.firstName);
        String address      = get(dto.address);
        String email        = get(dto.email);
        String phone        = get(dto.phone);
        String username     = get(dto.username);
        String password     = get(dto.password);
        String confirmPwd   = get(dto.confirmPassword);

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

        // Validation format courriel
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            toast("Adresse courriel invalide"); return;
        }

        // Validation téléphone : exactement 10 chiffres
        if (!phone.matches("[0-9]{10}")) {
            toast("Numéro de téléphone invalide (10 chiffres requis)"); return;
        }

        if (username.isEmpty()) {
            toast("Veuillez choisir un nom d'utilisateur"); return;
        }

        // Mot de passe minimum 6 caractères
        if (password.length() < 6) {
            toast("Le mot de passe doit contenir au moins 6 caractères"); return;
        }

        // Confirmation du mot de passe
        if (!password.equals(confirmPwd)) {
            toast("Les mots de passe ne correspondent pas"); return;
        }

        // ── Inscription en base (thread secondaire — Room interdit le thread principal) ──
        dto.role = UserRoles.CITIZEN;

        new Thread(() -> {
            try {
                UserRepository repo = new UserRepository(requireContext());
                repo.register(dto);

                requireActivity().runOnUiThread(() -> {
                    toast("Compte créé avec succès !");
                    ((AuthentificationActivity) requireActivity()).goToSignIn();
                });

            } catch (Exception e) {
                requireActivity().runOnUiThread(() ->
                        toast("Erreur : nom d'utilisateur ou courriel déjà utilisé")
                );
            }
        }).start();
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
