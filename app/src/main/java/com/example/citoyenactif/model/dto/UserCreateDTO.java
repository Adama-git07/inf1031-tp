package com.example.citoyenactif.model.dto;

import androidx.databinding.ObservableField;

import com.example.citoyenactif.model.enums.UserRoles;

/**
 * DTO utilisé lors de la création d'un compte citoyen.
 *
 * Les champs String utilisent ObservableField pour permettre
 * le DataBinding two-way (@={dto.field}) dans les layouts XML.
 * Cela évite les appels manuels à getText() dans les fragments.
 */
public class UserCreateDTO {

    /** Nom de famille */
    public ObservableField<String> lastName = new ObservableField<>("");

    /** Prénom */
    public ObservableField<String> firstName = new ObservableField<>("");

    /** Adresse postale */
    public ObservableField<String> address = new ObservableField<>("");

    /** Adresse courriel unique */
    public ObservableField<String> email = new ObservableField<>("");

    /** Numéro de téléphone (10 chiffres) */
    public ObservableField<String> phone = new ObservableField<>("");

    /** Nom d'utilisateur unique */
    public ObservableField<String> username = new ObservableField<>("");

    /** Mot de passe */
    public ObservableField<String> password = new ObservableField<>("");

    /** Confirmation du mot de passe (non sauvegardée en BD) */
    public ObservableField<String> confirmPassword = new ObservableField<>("");

    /** Rôle de l'utilisateur */
    public UserRoles role;
}
