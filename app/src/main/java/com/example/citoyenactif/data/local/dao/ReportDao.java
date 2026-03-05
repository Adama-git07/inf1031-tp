package com.example.citoyenactif.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.citoyenactif.data.local.entity.ReportEntity;
import java.util.List;

@Dao
public interface ReportDao {

    @Insert
    void insert(ReportEntity report);

    @Update
    void update(ReportEntity report);

    @Delete
    void delete(ReportEntity report);

    @Query("SELECT * FROM reports WHERE citizen_id = :citizenId ORDER BY created_at DESC")
    LiveData<List<ReportEntity>> getReportsByCitizen(int citizenId);

    @Query("SELECT * FROM reports ORDER BY created_at DESC")
    LiveData<List<ReportEntity>> getAllReports();

    @Query("SELECT * FROM reports WHERE status = 'EN_ATTENTE' ORDER BY created_at DESC")
    LiveData<List<ReportEntity>> getPendingReports();

    @Query("UPDATE reports SET status = :status WHERE id = :reportId")
    void updateStatus(long reportId, String status);
}