package com.example.citoyenactif.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

import com.example.citoyenactif.model.enums.ReportCategory;
import com.example.citoyenactif.model.enums.ReportStatus;

/** Entité Room représentant la table "reports" */
@Entity(tableName = "reports")
public class ReportEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String description;
    public ReportCategory category;
    public ReportStatus status;

    @ColumnInfo(name = "photo_uri")
    public String photoUri;

    public double latitude;
    public double longitude;

    @ColumnInfo(name = "citizen_id")
    public int citizenId;

    @ColumnInfo(name = "created_at")
    public long createdAt;
}