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

/**
 * Activité permettant à un citoyen de créer un nouveau signalement.
 *
 * Cette activité permet :
 * - de saisir un titre et une description
 * - de choisir une catégorie de problème
 * - de prendre une photo avec la caméra
 * - d'enregistrer le signalement dans la base de données via le Repository
 *
 * Elle utilise :
 * - DataBinding pour lier le layout XML
 * - ActivityResultLauncher pour gérer la caméra et les permissions
 * - Repository + ViewModel pour accéder aux données
 */
public class ReportActivity extends AppCompatActivity {

    /** Binding du layout report_activity.xml */
    private ReportActivityBinding binding;

    /** URI de la photo prise par la caméra */
    private Uri photoUri = null;

    /**
     * Launcher permettant d'ouvrir la caméra et de récupérer la photo.
     * Si la prise de photo réussit, l'image est affichée dans l'ImageView.
     */
    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && photoUri != null) {

                    // Affiche la photo dans l'interface
                    binding.ivPhoto.setImageURI(photoUri);
                    binding.ivPhoto.setVisibility(View.VISIBLE);

                    Toast.makeText(this, "Photo ajoutée ✓", Toast.LENGTH_SHORT).show();
                } else {

                    // Si l'utilisateur annule
                    photoUri = null;
                    Toast.makeText(this, "Photo annulée", Toast.LENGTH_SHORT).show();
                }
            });

    /**
     * Launcher permettant de demander la permission d'utiliser la caméra.
     * Si la permission est accordée, la caméra est ouverte.
     */
    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {

                    // Permission accordée → ouverture de la caméra
                    openCamera();
                } else {

                    // Permission refusée
                    Toast.makeText(this, "Permission caméra refusée", Toast.LENGTH_SHORT).show();
                }
            });

    /**
     * Méthode appelée à la création de l'activité.
     * Initialise l'interface et les listeners des boutons.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Liaison entre l'activité et le layout XML
        binding = DataBindingUtil.setContentView(this, R.layout.report_activity);

        // Bouton annuler → fermer l'activité
        binding.btnCancelReport.setOnClickListener(v -> finish());

        // Bouton sauvegarder → enregistrer le signalement
        binding.btnSaveReport.setOnClickListener(v -> saveReport());

        // Bouton prendre photo → vérifier permission caméra
        binding.btnTakePhoto.setOnClickListener(v -> checkCameraPermission());
    }

    /**
     * Vérifie si la permission caméra est accordée.
     * Si ce n'est pas le cas, la permission est demandée à l'utilisateur.
     */
    private void checkCameraPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {

            // Permission déjà accordée
            openCamera();

        } else {

            // Demande la permission
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    /**
     * Ouvre la caméra pour prendre une photo.
     * La photo sera sauvegardée dans un fichier temporaire.
     */
    private void openCamera() {

        try {

            // Création du fichier image
            File photoFile = createImageFile();

            // Création de l'URI sécurisée via FileProvider
            photoUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    photoFile
            );

            // Lancement de la caméra
            cameraLauncher.launch(photoUri);

        } catch (Exception e) {

            Toast.makeText(this, "Erreur caméra : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Crée un fichier image temporaire pour stocker la photo.
     *
     * @return fichier image créé
     * @throws IOException si une erreur se produit lors de la création
     */
    private File createImageFile() throws IOException {

        // Génère un nom unique avec timestamp
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

        // Dossier de stockage des images de l'application
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // Création du fichier image
        return File.createTempFile("REPORT_" + timestamp, ".jpg", storageDir);
    }

    /**
     * Sauvegarde le signalement saisi par le citoyen.
     *
     * Étapes :
     * 1. Validation des champs
     * 2. Création du DTO
     * 3. Enregistrement via le Repository
     */
    private void saveReport() {

        final String title = binding.etTitle.getText().toString().trim();
        final String description = binding.etDescription.getText().toString().trim();

        // Validation du titre
        if (title.isEmpty()) {
            Toast.makeText(this, "Veuillez saisir un titre", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validation de la description
        if (description.isEmpty()) {
            Toast.makeText(this, "Veuillez saisir une description", Toast.LENGTH_SHORT).show();
            return;
        }

        // Vérification de la catégorie
        int pos = binding.spCategory.getSelectedItemPosition();
        if (pos == 0) {
            Toast.makeText(this, "Veuillez choisir une catégorie", Toast.LENGTH_SHORT).show();
            return;
        }

        // Conversion de la position du spinner en enum
        final ReportCategory category = ReportCategory.values()[pos - 1];

        // Création du DTO
        ReportCreateDTO dto = new ReportCreateDTO();
        dto.title       = title;
        dto.description = description;
        dto.category    = category;
        dto.photoUri    = photoUri != null ? photoUri.toString() : null;
        dto.latitude    = 0.0;
        dto.longitude   = 0.0;
        dto.citizenId   = 0; // TODO : récupérer depuis SessionManager

        // Insertion dans la base de données (thread séparé)
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

    /**
     * ViewModel reliant l'interface utilisateur au Repository.
     *
     * Permet de gérer les données des signalements
     * en respectant l'architecture MVVM.
     */
    public static class ReportViewModel extends AndroidViewModel {

        private final ReportRepository repo;

        /** Liste observable de tous les signalements */
        private LiveData<List<ReportEntity>> allReports;

        /**
         * Constructeur du ViewModel.
         *
         * @param application contexte de l'application
         */
        public ReportViewModel(@NonNull Application application) {
            super(application);
            repo = new ReportRepository(application);
        }

        /**
         * Retourne tous les signalements.
         */
        public LiveData<List<ReportEntity>> getAllReports() {

            if (allReports == null) {
                allReports = repo.getAllReports();
            }

            return allReports;
        }

        /**
         * Retourne les signalements créés par un citoyen.
         *
         * @param citizenId identifiant du citoyen
         */
        public LiveData<List<ReportEntity>> getReportsByCitizen(int citizenId) {
            return repo.getReportsByCitizen(citizenId);
        }

        /**
         * Insère un nouveau signalement.
         */
        public void insert(ReportCreateDTO dto) {
            repo.insert(dto);
        }

        /**
         * Supprime un signalement.
         */
        public void delete(ReportEntity report) {
            repo.delete(report);
        }

        /**
         * Met à jour le statut d'un signalement.
         */
        public void updateStatus(int reportId, String status) {
            repo.updateStatus(reportId, status);
        }
    }
}