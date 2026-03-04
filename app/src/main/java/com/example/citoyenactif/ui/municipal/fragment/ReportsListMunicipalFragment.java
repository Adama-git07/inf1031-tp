package com.example.citoyenactif.ui.municipal.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.citoyenactif.databinding.ReportsListMunicipalFragmentBinding;

public class ReportsListMunicipalFragment extends Fragment {

    private ReportsListMunicipalFragmentBinding binder;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binder = ReportsListMunicipalFragmentBinding.inflate(inflater, container, false);
        return binder.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binder = null;
    }
}