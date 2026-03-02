package com.example.citoyenactif.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.citoyenactif.ui.AuthentificationActivity;
import com.example.citoyenactif.databinding.ResetPasswordFragBinding;

public class ResetPasswordFrag extends Fragment {
    private ResetPasswordFragBinding binder;

    public View onCreateView (@NonNull LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState){
        binder = ResetPasswordFragBinding.inflate(inflater, container, false);
        // Retourne à la page d'accueil
        binder.cancelButton.setOnClickListener(v -> {
            ((AuthentificationActivity) requireActivity()).goToSignIn();
        });
        return binder.getRoot();
    }

}
