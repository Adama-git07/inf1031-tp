package com.example.citoyenactif.ui.municipal;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.citoyenactif.ui.municipal.fragment.ReportsToTreatFrag;

public class MunicipalPageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, new ReportsToTreatFrag())
                    .commit();
        }
    }
}