package com.example.citoyenactif.data.local.converter;

import androidx.room.TypeConverter;
import com.example.citoyenactif.model.enums.ReportCategory;

/**
 * Convertisseur Room permettant de transformer l'enum ReportCategory
 * en String pour le stockage dans SQLite,
 * et inversement.
 */
public class ReportCategoryConverter {

    @TypeConverter //Convertit un ReportCategory en String.
    public static String fromCategory(ReportCategory category) {
        if (category == null) return null;
        return category.name();
    }
    @TypeConverter
    public static ReportCategory toCategory(String value) {
        if (value == null) return null;
        return ReportCategory.valueOf(value);
    }
}