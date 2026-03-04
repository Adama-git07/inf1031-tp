package com.example.citoyenactif.ui.citizen.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.citoyenactif.databinding.RepportsMapFragmentBinding;

public class ReportMapFragment extends Fragment {

    private RepportsMapFragmentBinding binder;

    public View onCreateView (@NonNull LayoutInflater inflater, ViewGroup container, Bundle saveInstanceStace){
        binder = RepportsMapFragmentBinding.inflate(inflater, container, false);
        return binder.getRoot();
    }

}
