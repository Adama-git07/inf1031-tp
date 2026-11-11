package com.example.citoyenactif.model.dto;

import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;

import com.example.citoyenactif.model.enums.ReportCategory;
import com.example.citoyenactif.model.enums.ReportStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO utilisé lors de la création ou modification d'un rapport de bris.*
 * Les champs texte utilisent ObservableField pour le DataBinding two-way
 * dans report_activity.xml (@={dto.title}, @={dto.description}).*
 * photoUris est une liste permettant d'attacher plusieurs photos par rapport.
 * Elles sont stockées en BD sous forme de chaîne séparée par "|".
 */
public class ReportCreateDTO {

    /** Titre du rapport — lié two-way au champ etTitle */
    public ObservableField<String> title = new ObservableField<>("");

    /** Description — liée two-way au champ etDescription */
    public ObservableField<String> description = new ObservableField<>("");

    /**
     * Position sélectionnée dans le Spinner — liée two-way (@={dto.categoryPosition}).
     * 0 = "Choisir une catégorie...", 1 = première vraie catégorie (ordinal() + 1).
     */
    public ObservableInt categoryPosition = new ObservableInt(0);

    /** Catégorie du bris */
    public ReportCategory category;

    /** Statut (généralement forcé à EN_ATTENTE par le Repository) */
    public ReportStatus status;

    /**
     * Liste des URIs des photos attachées au rapport.
     * Stockée en BD sous la forme : "uri1|uri2|uri3"
     */
    public List<String> photoUris = new ArrayList<>();

    /** Latitude GPS */
    public double latitude;

    /** Longitude GPS */
    public double longitude;

    /** Identifiant du citoyen auteur du rapport */
    public int citizenId;
}