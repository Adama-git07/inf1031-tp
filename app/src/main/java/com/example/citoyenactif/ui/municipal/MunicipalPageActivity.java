package com.example.citoyenactif.ui.municipal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.example.citoyenactif.R;
import com.example.citoyenactif.ui.BaseActivity;
import com.example.citoyenactif.databinding.MunicipalPageActivityBinding;
import com.example.citoyenactif.ui.authentification.AuthentificationActivity;
import com.example.citoyenactif.ui.citizen.fragment.ReportMapFragment;
import com.example.citoyenactif.ui.municipal.fragment.AgentProfilFragment;
import com.example.citoyenactif.ui.municipal.fragment.ReportsListMunicipalFragment;
import com.example.citoyenactif.util.SessionManager;

/**
 * Activité principale de l'espace municipal.
 *
 * Cette activité est accessible après la connexion d'un agent municipal.
 * Elle permet de naviguer entre différentes sections de l'application :
 *
 * - Liste des signalements
 * - Carte des signalements
 * - Profil de l'agent
 *
 * Elle utilise :
 * - DataBinding pour lier le layout XML
 * - Fragments pour afficher les différentes sections
 * - BottomNavigationView pour la navigation
 */
public class MunicipalPageActivity extends BaseActivity {

    /** Binding permettant d'accéder aux vues du layout municipal_page_activity.xml */
    private MunicipalPageActivityBinding binder;

    /**
     * Méthode appelée lors de la création de l'activité.
     *
     * Initialise :
     * - le DataBinding
     * - le fragment par défaut
     * - la navigation du bas (BottomNavigation)
     * - le bouton de déconnexion
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Cache la barre d'action pour une interface plus propre
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // Liaison entre l'activité et le layout XML via DataBinding
        binder = DataBindingUtil.setContentView(this, R.layout.municipal_page_activity);

        // Place le header au-dessus des fragments dans la hiérarchie des vues
        binder.header.bringToFront();
        binder.header.invalidate();

        /**
         * Listener du bouton logout.
         * Permet de déconnecter l'agent et de revenir
         * à l'écran d'authentification.
         */
        binder.btnLogoutMunicipal.setOnClickListener(v -> {
            Toast.makeText(this, "Logout click", Toast.LENGTH_SHORT).show();
            logout();
        });

        /**
         * Chargement du fragment par défaut
         * lorsque l'activité est ouverte pour la première fois.
         */
        if (savedInstanceState == null) {

            // Affiche la liste des signalements
            push(new ReportsListMunicipalFragment());

            // Met à jour la navigation du bas
            binder.bottomNavMunicipal.setSelectedItemId(R.id.nav_reports_list);
        }

        /**
         * Gestion de la navigation via le BottomNavigationView.
         *
         * Selon l'élément sélectionné, un fragment différent
         * est affiché dans le container.
         */
        binder.bottomNavMunicipal.setOnItemSelectedListener(item -> {

            Fragment selection;
            int id = item.getItemId();

            if (id == R.id.nav_agent_profile) {

                // Fragment profil agent
                selection = new AgentProfilFragment();

            } else if (id == R.id.nav_reports_map) {

                // Fragment carte des signalements
                selection = new ReportMapFragment();

            } else {

                // Fragment liste des signalements
                selection = new ReportsListMunicipalFragment();
            }

            // Remplace le fragment affiché
            push(selection);

            return true;
        });
    }

    /**
     * Remplace le fragment actuellement affiché
     * dans le container principal de l'activité.
     *
     * @param frag fragment à afficher
     */
    private void push(Fragment frag) {

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.municipal_fragment_container, frag)
                .commit();
    }

    /**
     * Déconnecte l'utilisateur actuellement connecté.
     *
     * Étapes :
     * 1. Suppression de la session avec SessionManager
     * 2. Redirection vers l'écran d'authentification
     * 3. Suppression de l'historique d'activités
     */
    private void logout() {

        // Suppression de la session utilisateur
        SessionManager.clear(this);

        // Redirection vers l'activité de connexion
        Intent intent = new Intent(this, AuthentificationActivity.class);

        // Supprime toutes les activités précédentes
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);

        // Termine l'activité actuelle
        finish();
    }
}