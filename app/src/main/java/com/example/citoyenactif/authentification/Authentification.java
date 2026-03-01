package com.example.citoyenactif.authentification;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.citoyenactif.R;
import com.example.citoyenactif.authentification.fragment.ResetPasswordFrag;
import com.example.citoyenactif.authentification.fragment.SignInFrag;
import com.example.citoyenactif.authentification.fragment.SignUpFrag;
import com.example.citoyenactif.databinding.AuthentificationBinding;

public class Authentification extends AppCompatActivity {
    private AuthentificationBinding binder;

    public void onCreate (Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        binder = DataBindingUtil.setContentView(this, R.layout.authentification);

        //charger sign_up_frag par defaut
        if ( saveInstanceState == null ){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.auth_frag_container, new SignUpFrag())
                    .commit();
        }

        //charger le sign_in_frag
        binder.tvCreateAccount.setOnClickListener( v -> {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.auth_frag_container, new SignInFrag())
                    .addToBackStack(null)
                    .commit();
        } );

        //charger reset_password_frag
        binder.tvCreateAccount.setOnClickListener( v -> {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.auth_frag_container, new ResetPasswordFrag())
                    .addToBackStack(null)
                    .commit();
        } );
    }
}
