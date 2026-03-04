package com.example.citoyenactif.data.local.converter;

import androidx.room.TypeConverter;
import com.example.citoyenactif.model.enums.ReportStatus;

/**
 * Convertisseur Room permettant de transformer l'enum ReportStatus
 * en String pour le stockage dans SQLite,
 * et inversement.
 */
public class ReportStatusConverter {

    @TypeConverter //Convertit un ReportStatus en String.
    public static String fromStatus(ReportStatus status) {
        if (status == null) return null;
        return status.name();
    }

    @TypeConverter
    public static ReportStatus toStatus(String value) {
        if (value == null) return null;
        return ReportStatus.valueOf(value);
    }
}