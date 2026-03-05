package com.example.citoyenactif.ui.citizen.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Fragment "Carte des rapports".
 *
 * Pour l'instant, ce fragment ne charge pas de layout XML et ne montre pas une vraie carte.
 * Il affiche simplement un TextView créé en Java pour indiquer que la carte sera implémentée plus tard.
 */
public class ReportMapFragment extends Fragment {

    /**
     * Méthode appelée pour créer et retourner la vue du fragment.
     *
     * @param inflater  permet normalement d'inflater un layout XML (non utilisé ici)
     * @param container parent dans lequel la vue du fragment sera insérée
     * @param savedInstanceState état sauvegardé du fragment (si existant)
     * @return la View racine du fragment (ici un TextView)
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        // Création d'un TextView directement en code (pas de XML)
        TextView tv = new TextView(getContext());

        // Texte temporaire affiché à l'utilisateur
        tv.setText("Carte (à implémenter)");

        // Taille du texte (18sp)
        tv.setTextSize(18f);

        // Padding interne (gauche, haut, droite, bas) en pixels
        // (À noter : idéalement utiliser dp/sp via resources pour s'adapter aux écrans)
        tv.setPadding(32, 32, 32, 32);

        // Retourne le TextView comme vue racine du fragment
        return tv;
    }
}