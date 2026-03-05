package com.example.citoyenactif.ui.citizen;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.citoyenactif.data.local.entity.ReportEntity;
import com.example.citoyenactif.data.repository.ReportRepository;
import com.example.citoyenactif.model.dto.ReportCreateDTO;

import java.util.List;

/** ViewModel liant l'UI citoyen au ReportRepository */
public class ReportViewModel extends AndroidViewModel {

    private final ReportRepository repo;
    private LiveData<List<ReportEntity>> allReports;

    public ReportViewModel(@NonNull Application application) {
        super(application);
        repo = new ReportRepository(application);
    }

    public LiveData<List<ReportEntity>> getAllReports() {
        if (allReports == null) {
            allReports = repo.getAllReports();
        }
        return allReports;
    }

    public LiveData<List<ReportEntity>> getReportsByCitizen(int citizenId) {
        return repo.getReportsByCitizen(citizenId);
    }

    public void insert(ReportCreateDTO dto) {
        repo.insert(dto);
    }

    public void delete(ReportEntity report) {
        repo.delete(report);
    }

    public void updateStatus(int reportId, String status) {
        repo.updateStatus(reportId, status);
    }
}