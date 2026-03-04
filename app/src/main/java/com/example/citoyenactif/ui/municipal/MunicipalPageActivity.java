package com.example.citoyenactif.ui.municipal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.example.citoyenactif.R;
import com.example.citoyenactif.databinding.MunicipalPageActivityBinding;
import com.example.citoyenactif.ui.authentification.AuthentificationActivity;
import com.example.citoyenactif.ui.citizen.fragment.ReportMapFragment;
import com.example.citoyenactif.ui.municipal.fragment.AgentProfilFragment;
import com.example.citoyenactif.ui.municipal.fragment.ReportsListMunicipalFragment;
//import com.example.citoyenactif.ui.municipal.fragment.ReportsMapMunicipalFragment;
import com.example.citoyenactif.util.SessionManager;

public class MunicipalPageActivity extends AppCompatActivity {

    private MunicipalPageActivityBinding binder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        binder = DataBindingUtil.setContentView(this, R.layout.municipal_page_activity);

        // ✅ IMPORTANT : header au-dessus du container/fragments
        binder.header.bringToFront();
        binder.header.invalidate();

        // ✅ Test: si tu vois ce toast, le click arrive
        binder.btnLogoutMunicipal.setOnClickListener(v -> {
            Toast.makeText(this, "Logout click", Toast.LENGTH_SHORT).show();
            logout();
        });

        // Fragment par défaut
        if (savedInstanceState == null) {
            push(new ReportsListMunicipalFragment());
            binder.bottomNavMunicipal.setSelectedItemId(R.id.nav_reports_list);
        }

        // Bottom nav
        binder.bottomNavMunicipal.setOnItemSelectedListener(item -> {
            Fragment selection;
            int id = item.getItemId();

            if (id == R.id.nav_agent_profile) {
                selection = new AgentProfilFragment();
            } else if (id == R.id.nav_reports_map) {
                selection = new ReportMapFragment();
            } else {
                selection = new ReportsListMunicipalFragment();
            }

            push(selection);
            return true;
        });
    }

    private void push(Fragment frag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.municipal_fragment_container, frag)
                .commit();
    }

    private void logout() {
        SessionManager.clear(this);

        Intent intent = new Intent(this, AuthentificationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}