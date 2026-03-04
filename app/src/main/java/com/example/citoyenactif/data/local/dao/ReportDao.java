package com.example.citoyenactif.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.citoyenactif.data.local.entity.ReportEntity;
import com.example.citoyenactif.model.enums.ReportStatus;

import java.util.List;

@Dao
public interface ReportDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(ReportEntity report);

    @Update
    int update( ReportEntity report );

    @Delete
    int delete(ReportEntity report );

    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    List<ReportEntity> getAll();

    @Query("SELECT * FROM reports WHERE createdByUserId = :userId ORDER BY createdAt DESC")
    List<ReportEntity> getByUser(long userId);

    @Query("SELECT * FROM reports WHERE status = :status ORDER BY createdAt DESC")
    List<ReportEntity> getByStatus(ReportStatus status);

    @Query("SELECT * FROM reports WHERE id = :reportId LIMIT 1")
    ReportEntity getById(long reportId);

    @Query("UPDATE reports SET status = :status WHERE id = :reportId")
    int updateStatus(long reportId, ReportStatus status);
}
