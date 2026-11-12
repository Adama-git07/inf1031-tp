package com.example.citoyenactif.ui.citizen;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.example.citoyenactif.R;
import com.example.citoyenactif.ui.BaseActivity;
import com.example.citoyenactif.databinding.CitizenPageActivityBinding;
import com.example.citoyenactif.ui.citizen.fragment.ProfilFragment;
import com.example.citoyenactif.ui.citizen.fragment.ReportMapFragment;
import com.example.citoyenactif.ui.citizen.fragment.ReportsListFragment;
import com.example.citoyenactif.util.SessionManager;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * CitizenPageActivity est la page principale côté citoyen.
 * Elle contient :
 * - Un FragmentContainerView (conteneur)
 * - Un BottomNavigationView (menu)
 * Elle charge les fragments suivants :
 * - ReportsListFragment (liste des rapports)
 * - ReportMapFragment (carte des rapports)
 * - ProfilFragment (profil utilisateur)
 *
 * Elle abonne aussi le citoyen à son sujet FCM pour recevoir les notifications
 * quand un agent traite l'un de ses signalements.
 *
 * Les marges (insets) évitent que le contenu passe sous la barre d'état,
 * la caméra ou la barre de navigation.
 */
public class CitizenPageActivity extends BaseActivity {

    private CitizenPageActivityBinding binder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Affichage bord à bord : l'écran va sous les barres système
        EdgeToEdge.enable(this);

        // DataBinding : citizen_page_activity.xml
        binder = DataBindingUtil.setContentView(this, R.layout.citizen_page_activity);

        // On ajoute des marges pour que le contenu reste visible
        ViewCompat.setOnApplyWindowInsetsListener(binder.citizenRoot, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && !hasPermission(Manifest.permission.POST_NOTIFICATIONS)) {
            requestPermissions(Manifest.permission.POST_NOTIFICATIONS);
        }

        // Abonnement au sujet FCM de ce citoyen (ex. citizen-12)
        long citizenId = SessionManager.getUserId(this);
        if (citizenId > 0) {
            FirebaseMessaging.getInstance()
                    .subscribeToTopic(SessionManager.citizenTopic(citizenId));
        }

        // Charger le fragment par défaut uniquement au premier lancement
        if (savedInstanceState == null) {
            push(new ReportsListFragment());
            binder.bottomNav.setSelectedItemId(R.id.nav_reports);
        }

        // Gérer la sélection sur le menu de navigation
        binder.bottomNav.setOnItemSelectedListener(item -> {

            Fragment selection;
            int id = item.getItemId();

            if (id == R.id.nav_reports) {
                selection = new ReportsListFragment();
            } else if (id == R.id.nav_map) {
                selection = new ReportMapFragment();
            } else {
                selection = new ProfilFragment();
            }

            push(selection);
            return true;
        });
    }

    /**
     * Charge un fragment dans le conteneur principal.
     * @param frag fragment à afficher
     */
    private void push(Fragment frag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.citizen_fragment_container, frag)
                .commit();
    }
}