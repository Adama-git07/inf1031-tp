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
import com.example.citoyenactif.ui.authentification.AuthentificationActivity;
import com.example.citoyenactif.util.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Fragment Profil côté citoyen.
 *
 * Rôle :
 * - Afficher les informations de l'utilisateur actuellement connecté
 * - Permettre la déconnexion (logout)
 *
 * Les infos sont récupérées depuis Room (UserRepository) en utilisant un userId
 * stocké dans SessionManager (SharedPreferences).
 */
public class ProfilFragment extends Fragment {

    /**
     * Binding généré par DataBinding pour accéder facilement aux vues
     * du layout profile_fragment.xml (ex: tvFullName, btnLogout...).
     */
    private ProfileFragmentBinding binder;

    /**
     * Méthode appelée pour créer la vue du fragment.
     * - Initialise le binding
     * - Branche le bouton de déconnexion
     * - Charge l'utilisateur connecté et affiche ses infos
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        // Liaison XML -> Java via DataBinding
        binder = ProfileFragmentBinding.inflate(inflater, container, false);

        // Clic sur le bouton "Déconnexion"
        binder.btnLogout.setOnClickListener(v -> logout());

        // Charger et afficher les informations du user connecté
        loadConnectedUser();

        // Retourner la vue racine du fragment
        return binder.getRoot();
    }

    /**
     * Charge l'utilisateur connecté à partir de son userId stocké en session.
     *
     * Étapes :
     * 1) Lire le userId depuis SessionManager
     * 2) Si userId invalide -> aucun user connecté -> logout
     * 3) Sinon, charger le UserEntity depuis Room dans un thread secondaire
     * 4) Revenir sur le thread UI pour afficher les infos dans la vue
     */
    private void loadConnectedUser() {

        // Récupérer l'id de l'utilisateur connecté depuis la session
        long userId = SessionManager.getUserId(requireContext());

        // Si pas d'utilisateur enregistré en session, on ne peut pas afficher le profil
        if (userId <= 0) {
            Toast.makeText(getContext(), "Aucun utilisateur connecté", Toast.LENGTH_SHORT).show();
            logout();
            return;
        }

        // Accès Room dans un thread secondaire (obligatoire pour éviter de bloquer l'UI)
        new Thread(() -> {

            // Repository -> DAO -> DB
            UserRepository repo = new UserRepository(requireContext());

            // Cherche le user dans la table users
            UserEntity user = repo.getById(userId);

            // Retour sur le thread principal pour mettre à jour les vues
            requireActivity().runOnUiThread(() -> {

                // Si l'user n'existe plus en BD (ex: DB supprimée), on force la déconnexion
                if (user == null) {
                    Toast.makeText(getContext(), "Utilisateur introuvable", Toast.LENGTH_SHORT).show();
                    logout();
                    return;
                }

                // Affichage des infos dans la carte profil
                // Ici on met username dans tvFullName car UserEntity n'a pas (firstName/lastName)
                binder.tvFullName.setText(user.username);

                // Détails username/email
                binder.tvUsername.setText("Username : " + user.username);
                binder.tvEmail.setText("Email : " + user.email);

                // Formatage de la date de création du compte
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .format(new Date(user.createdAt));
                binder.tvCreatedAt.setText("Compte créé : " + date);
            });

        }).start();
    }

    /**
     * Déconnecte l'utilisateur :
     * - Efface la session (SharedPreferences)
     * - Redirige vers AuthentificationActivity
     * - Nettoie la pile d'activités (impossible de revenir en arrière)
     */
    private void logout() {

        // Supprime les infos de session (userId, etc.)
        SessionManager.clear(requireContext());

        // Revenir à l'écran d'authentification
        Intent intent = new Intent(requireActivity(), AuthentificationActivity.class);

        // CLEAR_TASK + NEW_TASK : supprime l'historique des activités
        // => l'utilisateur ne peut pas revenir au profil avec "Retour"
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);

        // Fermer l'activité actuelle qui héberge les fragments (CitizenPageActivity)
        requireActivity().finish();
    }

    /**
     * Appelé quand la vue du fragment est détruite.
     * Bonne pratique : mettre binder à null pour éviter les fuites mémoire.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binder = null;
    }
}