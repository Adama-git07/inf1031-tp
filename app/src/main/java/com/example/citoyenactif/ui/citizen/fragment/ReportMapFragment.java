package com.example.citoyenactif.ui.citizen.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.citoyenactif.databinding.RepportsMapFragmentBinding;

public class ReportMapFragment extends Fragment {

    private RepportsMapFragmentBinding binder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binder = RepportsMapFragmentBinding.inflate(inflater, container, false);
        return binder.getRoot();
    }

    // TODO: intégrer Google Maps et afficher les marqueurs ReportMarkerDTO
}