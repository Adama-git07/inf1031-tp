package com.example.citoyenactif.ui.municipal.fragment;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Afficher la liste des bris signalés qui doivent être traités par les agents municipaux.
 * C’est en quelque sorte la boîte de travail des agents.
 */

public class ReportsToTreatFrag extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Layout simple temporaire
        TextView tv = new TextView(getContext());
        tv.setText("Rapports à traiter - À implémenter");
        tv.setTextSize(18f);
        tv.setPadding(32, 32, 32, 32);
        return tv;
    }
}
