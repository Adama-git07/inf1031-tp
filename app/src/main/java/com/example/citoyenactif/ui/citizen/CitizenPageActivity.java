package com.example.citoyenactif.ui.citizen;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.example.citoyenactif.R;
import com.example.citoyenactif.ui.BaseActivity;
import com.example.citoyenactif.databinding.CitizenPageActivityBinding;
import com.example.citoyenactif.ui.citizen.fragment.ProfilFragment;
import com.example.citoyenactif.ui.citizen.fragment.ReportMapFragment;
import com.example.citoyenactif.ui.citizen.fragment.ReportsListFragment;

/**
 * CitizenPageActivity est la page principale côté citoyen.
 *
 * Elle contient :
 * - Un FragmentContainerView (conteneur)
 * - Un BottomNavigationView (menu)
 *
 * Elle charge les fragments suivants :
 * - ReportsListFragment (liste des rapports)
 * - ReportMapFragment (carte des rapports)
 * - ProfilFragment (profil utilisateur)
 */
public class CitizenPageActivity extends BaseActivity {

    private CitizenPageActivityBinding binder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // DataBinding : citizen_page_activity.xml
        binder = DataBindingUtil.setContentView(this, R.layout.citizen_page_activity);

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
     *
     * @param frag fragment à afficher
     */
    private void push(Fragment frag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.citizen_fragment_container, frag)
                .commit();
    }
}