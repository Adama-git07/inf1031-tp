package com.example.citoyenactif.citizen;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.example.citoyenactif.R;
import com.example.citoyenactif.citizen.fragment.ProfilFragment;
import com.example.citoyenactif.citizen.fragment.ReportsListFragment;
import com.example.citoyenactif.citizen.fragment.RepportMapFragment;
import com.example.citoyenactif.databinding.CitizenPageActivityBinding;

/**
 * Class CitizenPageActivity affiche un menu (BottomNavigationView)
 * charge un fragment dans un FragmentContainerView
 */
public class CitizenPageActivity extends AppCompatActivity {

    private CitizenPageActivityBinding binder;

    protected void onCreate ( Bundle saveInstanceState){

        super.onCreate(saveInstanceState);
        binder = DataBindingUtil.setContentView( this, R.layout.citizen_page_activity );

        //charger le fragment de la liste des rapports par defaut
        if ( saveInstanceState == null){
            push( new ReportsListFragment());
            binder.bottomNav.setSelectedItemId(R.id.nav_reports);
        }
        //recuperation des selection sur le menu de navigation
        binder.bottomNav.setOnItemSelectedListener( item -> {
            Fragment selection;
            int id = item.getItemId();

            if ( id == R.id.nav_reports){
                selection = new ReportsListFragment();
            } else if ( id == R.id.nav_map){
                selection = new RepportMapFragment();
            } else {
                selection = new ProfilFragment();
            }
            push(selection);
            return true;
        });
    }

    private void push(Fragment frag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.citizen_fragment_container, frag)
                .commit();
    }
}
