package com.example.citoyenactif.model.dto;

/** DTO représentant un marqueur de rapport sur la carte */
public class ReportMarkerDTO {

    public int id;
    public String title;
    public String category;
    public String status;
    public double latitude;
    public double longitude;

    public ReportMarkerDTO(int id, String title, String category, String status,
                           double latitude, double longitude) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}