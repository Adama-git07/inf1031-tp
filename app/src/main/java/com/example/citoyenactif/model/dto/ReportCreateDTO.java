package com.example.citoyenactif.model.dto;

import com.example.citoyenactif.model.enums.ReportCategory;
import com.example.citoyenactif.model.enums.ReportStatus;

/** DTO utilisé lors de la création d'un rapport de bris */
public class ReportCreateDTO {

    public String title;
    public String description;
    public ReportCategory category;
    public ReportStatus status;
    public String photoUri;
    public double latitude;
    public double longitude;
    public int citizenId;
}