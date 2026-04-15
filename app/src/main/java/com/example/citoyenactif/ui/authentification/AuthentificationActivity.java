package com.example.citoyenactif.ui.authentification;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.example.citoyenactif.R;
import com.example.citoyenactif.ui.BaseActivity;
import com.example.citoyenactif.databinding.AuthentificationActivityBinding;
import com.example.citoyenactif.ui.authentification.fragment.ResetPasswordFrag;
import com.example.citoyenactif.ui.authentification.fragment.SignUpFrag;
import com.example.citoyenactif.ui.authentification.fragment.SignInFrag;
import com.example.citoyenactif.util.SeedData;

/**
 * Activité d'authentification.
 *
 * Rôle unique : héberger le FragmentContainerView et exposer
 * des méthodes de navigation entre les fragments d'authentification.
 *
 * Tous les liens et formulaires vivent dans les fragments eux-mêmes.
 */
public class AuthentificationActivity extends BaseActivity {

    private AuthentificationActivityBinding binder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binder = DataBindingUtil.setContentView(this, R.layout.authentification_activity);

        // Charger le fragment de connexion par défaut
        if (savedInstanceState == null) {
            push(new SignInFrag(), false);
        }

        SeedData.seedAgents();
    }

    // ── Navigation publique — appelée depuis les fragments ────────────────────

    /** Ouvre le formulaire d'inscription (SignUpFrag). */
    public void goToSignUp() {
        push(new SignUpFrag(), true);
    }

    /** Ouvre l'écran de réinitialisation de mot de passe (ResetPasswordFrag). */
    public void goToResetPassword() {
        push(new ResetPasswordFrag(), true);
    }

    /**
     * Revient à l'écran de connexion (SignInFrag) sans l'ajouter au BackStack.
     * Garantit que "Retour" ne laisse pas l'écran vide.
     */
    public void goToSignIn() {
        push(new SignInFrag(), false);
    }

    // ── Méthode interne ───────────────────────────────────────────────────────

    /**
     * Charge un fragment dans le conteneur.
     *
     * @param frag           fragment à afficher
     * @param addToBackStack true = l'utilisateur peut revenir en arrière
     */
    private void push(Fragment frag, boolean addToBackStack) {
        if (addToBackStack) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.auth_frag_container, frag)
                    .addToBackStack(null)
                    .commit();
        } else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.auth_frag_container, frag)
                    .commit();
        }
    }
}
