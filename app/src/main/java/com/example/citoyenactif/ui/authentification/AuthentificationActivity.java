package com.example.citoyenactif.ui.authentification;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;

import com.example.citoyenactif.R;
import com.example.citoyenactif.databinding.AuthentificationActivityBinding;
import com.example.citoyenactif.ui.authentification.fragment.ResetPasswordFrag;
import com.example.citoyenactif.ui.authentification.fragment.SignUpFrag;
import com.example.citoyenactif.ui.authentification.fragment.SignInFrag;
import com.example.citoyenactif.util.SeedData;


public class AuthentificationActivity extends AppCompatActivity {
    private AuthentificationActivityBinding binder;

    public void onCreate (Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        binder = DataBindingUtil.setContentView(this, R.layout.authentification_activity);

        // 1) Charger le fragment de connexion par défaut (SignIn)
        if (saveInstanceState == null) {
            push(new SignInFrag(), false);
        }

        // 2) Lien "Créer un compte" -> SignUp
        binder.tvCreateAccount.setOnClickListener(v ->
                push(new SignUpFrag(), true)
        );

        // 3) Lien "Mot de passe oublié ?" -> ResetPassword
        binder.tvForgotPassword.setOnClickListener(v ->
                push(new ResetPasswordFrag(), true)
        );

        SeedData.seedAgents(this); //ajoute les agents

    }

    /**
     * Charge un fragment dans le FragmentContainerView.
     *
     * @param frag fragment à afficher
     * @param addToBackStack indique si le fragment doit être ajouté à la pile de navigation (BackStack)
     *
     * Règle importante :
     * - Le premier fragment chargé dans l'activité (fragment initial)
     *   NE DOIT PAS être ajouté au BackStack.
     *   Sinon, lorsque l'utilisateur appuie sur le bouton "Retour",
     *   l'écran deviendra vide car il n'y aura plus de fragment à afficher.
     *
     * - Les fragments chargés suite à une action utilisateur
     *   (ex : SignUp ou ResetPassword) doivent être ajoutés au BackStack
     *   afin de permettre le retour vers l'écran précédent.
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

    /**
     * Redirige l'utilisateur vers le fragment de connexion (SignInFrag).
     *
     * Cette méthode est appelée depuis un fragment (ex : SignUpFrag ou ResetPasswordFrag)
     * lorsque l'utilisateur souhaite annuler l'action en cours et revenir
     * à l'écran de connexion.
     *
     * Le fragment SignIn est chargé sans être ajouté au BackStack
     * (addToBackStack = false), car il représente l'écran de base
     * de l'activité d'authentification.
     *
     * Cela garantit qu'un appui sur le bouton "Retour"
     * ne laissera pas l'écran vide.
     */
    public void goToSignIn() {
        push(new SignInFrag(), false);
    }
}
