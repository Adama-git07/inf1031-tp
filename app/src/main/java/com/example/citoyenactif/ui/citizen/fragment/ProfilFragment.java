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
import androidx.lifecycle.ViewModelProvider;

import com.example.citoyenactif.databinding.ProfileFragmentBinding;
import com.example.citoyenactif.model.enums.UserRoles;
import com.example.citoyenactif.ui.authentification.AuthentificationActivity;
import com.example.citoyenactif.ui.viewmodel.ProfileViewModel;
import com.example.citoyenactif.util.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Fragment Profil citoyen (afficher les informations).
 *
 * Affiche tous les champs de l'utilisateur :
 *   - Nom complet (prénom + nom), username, rôle
 *   - Email, téléphone, adresse, date de création
 *
 * Architecture MVVM :
 *   - ProfileViewModel charge l'utilisateur depuis Room en arrière-plan.
 *   - Le Fragment observe le LiveData et peuple les vues.
 *   - Aucun accès direct au repository ni création de Thread dans le Fragment.
 */
public class ProfilFragment extends Fragment {

    private ProfileFragmentBinding binder;

    /** ViewModel du profil citoyen */
    private ProfileViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        binder = ProfileFragmentBinding.inflate(inflater, container, false);

        // Obtenir le ViewModel
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        binder.btnLogout.setOnClickListener(v -> logout());

        // ── Observation des données utilisateur ───────────────────────────────
        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) {
                Toast.makeText(getContext(), "Utilisateur introuvable", Toast.LENGTH_SHORT).show();
                logout();
                return;
            }

            // ── Carte 1 : Avatar + Nom ────────────────────────────────────────
            String firstName = user.firstName != null ? user.firstName : "";
            String lastName  = user.lastName  != null ? user.lastName  : "";
            String fullName  = (firstName + " " + lastName).trim();
            if (fullName.isEmpty()) fullName = user.username;
            binder.tvFullName.setText(fullName);

            binder.tvUsername.setText("@" + user.username);

            String roleLabel = (user.role == UserRoles.CITIZEN) ? "Citoyen" : "Agent municipal";
            binder.tvRole.setText(roleLabel);

            // ── Carte 2 : Détails ─────────────────────────────────────────────
            binder.tvEmail.setText(user.email != null ? user.email : "—");
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

        // Déclencher le chargement
        loadConnectedUser();

        return binder.getRoot();
    }

    /**
     * Lance le chargement de l'utilisateur connecté via le ViewModel.
     */
    private void loadConnectedUser() {
        long userId = SessionManager.getUserId(requireContext());

        if (userId <= 0) {
            Toast.makeText(getContext(), "Aucun utilisateur connecté", Toast.LENGTH_SHORT).show();
            logout();
            return;
        }

        // Délégation au ViewModel — aucun Thread ici
        viewModel.loadUser(userId);
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
