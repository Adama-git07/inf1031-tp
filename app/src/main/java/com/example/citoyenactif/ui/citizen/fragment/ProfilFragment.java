package com.example.citoyenactif.ui.citizen.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.citoyenactif.databinding.ProfileFragmentBinding;
import com.example.citoyenactif.model.enums.UserRoles;
import com.example.citoyenactif.ui.authentification.AuthentificationActivity;
import com.example.citoyenactif.ui.binding.ProfileUiModel;
import com.example.citoyenactif.ui.viewmodel.ProfileViewModel;
import com.example.citoyenactif.util.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Fragment Profil citoyen (afficher les informations).
 *
 * Utilise DataBinding :
 *   - Le layout profile_fragment.xml déclare <variable name="profile"/>
 *   - Les textes se remplissent avec @{profile.xxx}
 *   - Le Fragment prépare les textes dans un ProfileUiModel, sans setText()
 *
 * Architecture MVVM :
 *   - ProfileViewModel charge l'utilisateur depuis Room en arrière-plan.
 *   - Le Fragment observe le LiveData et donne le modèle au binding.
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
        binder.setLifecycleOwner(getViewLifecycleOwner());

        // Obtenir le ViewModel
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        binder.btnLogout.setOnClickListener(v -> logout());

        // Quand l'utilisateur est chargé, on prépare les textes et on les donne au layout
        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) {
                Toast.makeText(getContext(), "Utilisateur introuvable", Toast.LENGTH_SHORT).show();
                logout();
                return;
            }

            // Nom complet (ou username si vide)
            String firstName = user.firstName != null ? user.firstName : "";
            String lastName  = user.lastName  != null ? user.lastName  : "";
            String fullName  = (firstName + " " + lastName).trim();
            if (fullName.isEmpty()) fullName = user.username;

            String roleLabel = (user.role == UserRoles.CITIZEN) ? "Citoyen" : "Agent municipal";

            String date = new SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH)
                    .format(new Date(user.createdAt));

            ProfileUiModel profile = new ProfileUiModel(
                    fullName,
                    "@" + user.username,
                    roleLabel,
                    user.email != null ? user.email : "—",
                    (user.phone != null && !user.phone.isEmpty()) ? user.phone : "—",
                    (user.address != null && !user.address.isEmpty()) ? user.address : "—",
                    "Membre depuis le " + date
            );

            // Une seule ligne : le layout met tous les textes à jour
            binder.setProfile(profile);
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

        // Délégation au ViewModel, aucun Thread ici
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