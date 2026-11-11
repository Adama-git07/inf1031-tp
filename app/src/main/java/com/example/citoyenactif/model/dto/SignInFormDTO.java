package com.example.citoyenactif.model.dto;

import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

/**
 * Formulaire de connexion (citoyen OU agent) lié au layout par DataBinding two-way.
 *
 * Dans sign_in_frag.xml :
 *   - chaque champ est lié avec @={dto.xxx}  (la saisie arrive toute seule dans le DTO)
 *   - le RadioButton "Agent" est lié avec @={dto.agent}
 *   - la visibilité des deux formulaires dépend de dto.agent
 * Le fragment n'a donc plus besoin de getText() ni de showCitizenForm()/showAgentForm().
 */
public class SignInFormDTO {

    // ── Formulaire citoyen ────────────────────────────────────────────────────
    public final ObservableField<String> username = new ObservableField<>("");
    public final ObservableField<String> password = new ObservableField<>("");

    // ── Formulaire agent ──────────────────────────────────────────────────────
    public final ObservableField<String> agentNumber   = new ObservableField<>("");
    public final ObservableField<String> agentPassword = new ObservableField<>("");

    /** true = l'onglet "Agent municipal" est sélectionné. */
    public final ObservableBoolean agent = new ObservableBoolean(false);

    public SignInFormDTO() {
        // Comme avant : quand on change de type de compte, on vide l'autre formulaire
        agent.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                clearInactiveForm();
            }
        });
    }

    /** Vide les champs du formulaire qui vient d'être masqué. */
    private void clearInactiveForm() {
        if (agent.get()) {
            username.set("");
            password.set("");
        } else {
            agentNumber.set("");
            agentPassword.set("");
        }
    }
}
