package com.example.citoyenactif.authentification.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.citoyenactif.databinding.ResetPasswordFragBinding;

public class ResetPasswordFrag extends Fragment {
    private ResetPasswordFragBinding binder;

    public View onCreateView (@NonNull LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState){
        return ResetPasswordFragBinding.inflate(inflater, container,false).getRoot();
    }

}
