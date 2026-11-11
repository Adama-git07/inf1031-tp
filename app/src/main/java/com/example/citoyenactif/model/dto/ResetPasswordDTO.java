package com.example.citoyenactif.model.dto;

import androidx.databinding.ObservableField;

/**
 * Champs du formulaire "réinitialiser le mot de passe", liés au layout
 * par DataBinding two-way (@={dto.username}, @={dto.password}, @={dto.confirmPassword}).
 */
public class ResetPasswordDTO {

    /** Nom d'utilisateur du compte à réinitialiser */
    public final ObservableField<String> username = new ObservableField<>("");

    /** Nouveau mot de passe */
    public final ObservableField<String> password = new ObservableField<>("");

    /** Confirmation du nouveau mot de passe */
    public final ObservableField<String> confirmPassword = new ObservableField<>("");
}