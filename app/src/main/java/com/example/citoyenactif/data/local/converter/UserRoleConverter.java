package com.example.citoyenactif.data.local.converter;

import androidx.room.TypeConverter;

import com.example.citoyenactif.model.enums.UserRoles;

/**
 * Convertisseur Room permettant de transformer l'enum UserRole
 * en String pour le stockage dans SQLite,
 * et inversement.
 */
public class UserRoleConverter {

    /**
     * Convertit un UserRole en String.
     * Utilisé automatiquement par Room lors de l'insertion en base.
     */
    @TypeConverter
    public static String fromRole(UserRoles role) {

        if (role == null) {
            return null;
        }

        return role.name();
    }

    /**
     * Convertit une String lue en base en UserRole.
     * Utilisé automatiquement par Room lors de la lecture.
     */
    @TypeConverter
    public static UserRoles toRole(String value) {

        if (value == null) {
            return null;
        }

        return UserRoles.valueOf(value);
    }
}
