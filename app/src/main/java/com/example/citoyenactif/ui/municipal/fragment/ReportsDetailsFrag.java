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
 * Afficher toutes les informations d’un signalement spécifique.
 * Quand l’agent clique sur un signalement dans la liste, il arrive ici.
 * ce fragment permet de voir les details d'un report
 */
public class ReportsDetailsFrag extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        TextView tv = new TextView(getContext());
        tv.setText("Détails du rapport - À implémenter");
        tv.setTextSize(18f);
        tv.setPadding(32, 32, 32, 32);
        return tv;
    }
}
