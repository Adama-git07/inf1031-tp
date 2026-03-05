package com.example.citoyenactif.ui.citizen.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ReportMapFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        TextView tv = new TextView(getContext());
        tv.setText("Carte (à implémenter)");
        tv.setTextSize(18f);
        tv.setPadding(32, 32, 32, 32);
        return tv;
    }
}