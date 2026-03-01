package com.example.citoyenactif.authentification.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.citoyenactif.databinding.SignUpFragBinding;

public class SignUpFrag extends Fragment {
    private SignUpFragBinding binder;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle saveInstanceState){
        binder = SignUpFragBinding.inflate(inflater, container, false);
        return binder.getRoot();
    }
}
