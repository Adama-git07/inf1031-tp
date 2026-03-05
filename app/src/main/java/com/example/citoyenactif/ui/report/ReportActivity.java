package com.example.citoyenactif.ui.report;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.citoyenactif.R;
import com.example.citoyenactif.data.local.entity.ReportEntity;
import com.example.citoyenactif.data.repository.ReportRepository;
import com.example.citoyenactif.databinding.ReportActivityBinding;
import com.example.citoyenactif.model.dto.ReportCreateDTO;
import com.example.citoyenactif.model.enums.ReportCategory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportActivity extends AppCompatActivity {

    private ReportActivityBinding binding;
    private Uri photoUri = null;

    // ✅ Launcher caméra
    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && photoUri != null) {
                    binding.ivPhoto.setImageURI(photoUri);
                    binding.ivPhoto.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Photo ajoutée ✓", Toast.LENGTH_SHORT).show();
                } else {
                    photoUri = null;
                    Toast.makeText(this, "Photo annulée", Toast.LENGTH_SHORT).show();
                }
            });

    // ✅ Launcher permission caméra
    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    openCamera(); // permission accordée → ouvre la caméra
                } else {
                    Toast.makeText(this, "Permission caméra refusée", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.report_activity);

        binding.btnCancelReport.setOnClickListener(v -> finish());
        binding.btnSaveReport.setOnClickListener(v -> saveReport());

        // ✅ Vérifier permission avant d'ouvrir la caméra
        binding.btnTakePhoto.setOnClickListener(v -> checkCameraPermission());
    }

    /**
     * Vérifie si la permission caméra est accordée,
     * sinon la demande au runtime.
     */
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera(); // déjà accordée
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA); // demander
        }
    }

    private void openCamera() {
        try {
            File photoFile = createImageFile();
            photoUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    photoFile
            );
            cameraLauncher.launch(photoUri);
        } catch (Exception e) {
            Toast.makeText(this, "Erreur caméra : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private File createImageFile() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile("REPORT_" + timestamp, ".jpg", storageDir);
    }

    private void saveReport() {

        final String title = binding.etTitle.getText().toString().trim();
        final String description = binding.etDescription.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Veuillez saisir un titre", Toast.LENGTH_SHORT).show();
            return;
        }

        if (description.isEmpty()) {
            Toast.makeText(this, "Veuillez saisir une description", Toast.LENGTH_SHORT).show();
            return;
        }

        int pos = binding.spCategory.getSelectedItemPosition();
        if (pos == 0) {
            Toast.makeText(this, "Veuillez choisir une catégorie", Toast.LENGTH_SHORT).show();
            return;
        }

        final ReportCategory category = ReportCategory.values()[pos - 1];

        ReportCreateDTO dto = new ReportCreateDTO();
        dto.title       = title;
        dto.description = description;
        dto.category    = category;
        dto.photoUri    = photoUri != null ? photoUri.toString() : null;
        dto.latitude    = 0.0;
        dto.longitude   = 0.0;
        dto.citizenId   = 0; // TODO : SessionManager

        new Thread(() -> {
            try {
                ReportRepository repo = new ReportRepository(getApplication());
                repo.insert(dto);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Rapport enregistré !", Toast.LENGTH_SHORT).show();
                    finish();
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    /** ViewModel liant l'UI citoyen au ReportRepository */
    public static class ReportViewModel extends AndroidViewModel {

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
}