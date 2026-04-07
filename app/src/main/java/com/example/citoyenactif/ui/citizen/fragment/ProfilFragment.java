package com.example.citoyenactif.ui.citizen.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.citoyenactif.data.local.entity.UserEntity;
import com.example.citoyenactif.data.repository.UserRepository;
import com.example.citoyenactif.databinding.ProfileFragmentBinding;
import com.example.citoyenactif.model.enums.UserRoles;
import com.example.citoyenactif.ui.authentification.AuthentificationActivity;
import com.example.citoyenactif.util.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Fragment Profil citoyen — Fix #5.
 *
 * Affiche maintenant TOUS les champs de l'utilisateur :
 *   - Nom complet (prénom + nom)
 *   - Username
 *   - Rôle
 *   - Email
 *   - Téléphone
 *   - Adresse
 *   - Date de création du compte
 *
 * Le layout est restructuré en deux cartes :
 *   1. Avatar + nom/username/rôle
 *   2. Détails (email, téléphone, adresse, date)
 */
public class ProfilFragment extends Fragment {

    private ProfileFragmentBinding binder;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        binder = ProfileFragmentBinding.inflate(inflater, container, false);

        binder.btnLogout.setOnClickListener(v -> logout());
        loadConnectedUser();

        return binder.getRoot();
    }

    /**
     * Charge l'utilisateur connecté depuis Room et peuple toutes les vues.
     */
    private void loadConnectedUser() {

        long userId = SessionManager.getUserId(requireContext());

        if (userId <= 0) {
            Toast.makeText(getContext(), "Aucun utilisateur connecté", Toast.LENGTH_SHORT).show();
            logout();
            return;
        }

        new Thread(() -> {

            UserRepository repo = new UserRepository(requireContext());
            UserEntity user = repo.getById(userId);

            requireActivity().runOnUiThread(() -> {

                if (user == null) {
                    Toast.makeText(getContext(), "Utilisateur introuvable", Toast.LENGTH_SHORT).show();
                    logout();
                    return;
                }

                // ── Carte 1 : Avatar + Nom ───────────────────────────────────

                // Nom complet : Prénom + Nom (avec fallbacks si null)
                String firstName = user.firstName != null ? user.firstName : "";
                String lastName  = user.lastName  != null ? user.lastName  : "";
                String fullName  = (firstName + " " + lastName).trim();
                if (fullName.isEmpty()) fullName = user.username; // fallback username
                binder.tvFullName.setText(fullName);

                binder.tvUsername.setText("@" + user.username);

                // Rôle lisible
                String roleLabel = (user.role == UserRoles.CITIZEN) ? "Citoyen" : "Agent municipal";
                binder.tvRole.setText(roleLabel);

                // ── Carte 2 : Détails ────────────────────────────────────────

                binder.tvEmail.setText(
                        user.email != null ? user.email : "—"
                );
                binder.tvPhone.setText(
                        (user.phone != null && !user.phone.isEmpty()) ? user.phone : "—"
                );
                binder.tvAddress.setText(
                        (user.address != null && !user.address.isEmpty()) ? user.address : "—"
                );

                String date = new SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH)
                        .format(new Date(user.createdAt));
                binder.tvCreatedAt.setText("Membre depuis le " + date);
            });

        }).start();
    }

    /**
     * Déconnecte l'utilisateur et redirige vers l'écran d'authentification.
     */
    private void logout() {
        SessionManager.clear(requireContext());
        Intent intent = new Intent(requireActivity(), AuthentificationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binder = null;
    }
}
