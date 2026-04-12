package com.example.citoyenactif.ui.report;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.example.citoyenactif.R;
import com.example.citoyenactif.data.local.entity.ReportEntity;
import com.example.citoyenactif.databinding.ReportActivityBinding;
import com.example.citoyenactif.model.dto.ReportCreateDTO;
import com.example.citoyenactif.model.enums.ReportCategory;
import com.example.citoyenactif.ui.viewmodel.ReportViewModel;
import com.example.citoyenactif.util.SessionManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Activité de gestion des rapports de bris.
 *
 * Trois modes :
 *   • CREATE  : formulaire vide → "Enregistrer" crée un nouveau rapport
 *   • VIEW    : champs remplis + non-éditables → bouton "Modifier"
 *   • EDIT    : champs éditables → "Enregistrer" met à jour le rapport
 *
 * Architecture MVVM :
 *   - ReportViewModel (standalone) gère toutes les opérations Room.
 *   - ReportActivity observe le LiveData getById() pour charger un rapport existant.
 *   - Les opérations d'écriture (insert, update) restent en Thread secondaire
 *     car elles nécessitent un retour immédiat (finish()) après succès.
 *
 * Multi-photos :
 *   - Chaque photo est ajoutée à la liste photoUris
 *   - Stockée en BD comme "uri1|uri2|..." dans la colonne photo_uri
 */
public class ReportActivity extends AppCompatActivity {

    // ── Constantes de mode ──────────────────────────────────────────────────
    private static final int MODE_CREATE = 0;
    private static final int MODE_VIEW   = 1;
    private static final int MODE_EDIT   = 2;

    /** Extra passé par ReportsListFragment lors d'un clic sur un rapport. */
    public static final String EXTRA_REPORT_ID = "report_id";

    // ── État ────────────────────────────────────────────────────────────────
    private ReportActivityBinding binding;
    private ReportCreateDTO dto;
    private int mode = MODE_CREATE;
    private ReportEntity currentReport = null;

    /** ViewModel pour les rapports */
    private ReportViewModel viewModel;

    /** Liste des URIs des photos attachées. */
    private final List<String> photoUris = new ArrayList<>();

    /** URI temporaire de la photo en cours de capture. */
    private Uri pendingPhotoUri = null;

    // ── Launchers ────────────────────────────────────────────────────────────

    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && pendingPhotoUri != null) {
                    photoUris.add(pendingPhotoUri.toString());
                    refreshPhotoStrip();
                    Toast.makeText(this, "Photo ajoutée ✓", Toast.LENGTH_SHORT).show();
                }
                pendingPhotoUri = null;
            });

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) openCamera();
                else Toast.makeText(this, "Permission caméra refusée", Toast.LENGTH_SHORT).show();
            });

    // ── Lifecycle ────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dto = new ReportCreateDTO();
        binding = DataBindingUtil.setContentView(this, R.layout.report_activity);
        binding.setDto(dto);
        binding.setLifecycleOwner(this);

        // Obtenir le ViewModel (standalone)
        viewModel = new ViewModelProvider(this).get(ReportViewModel.class);

        // Déterminer le mode selon la présence de EXTRA_REPORT_ID
        int reportId = getIntent().getIntExtra(EXTRA_REPORT_ID, -1);

        if (reportId != -1) {
            mode = MODE_VIEW;
            loadReport(reportId);
        } else {
            mode = MODE_CREATE;
            applyMode();
        }

        // ── Listeners ───────────────────────────────────────────────────────

        binding.btnCancelReport.setOnClickListener(v -> {
            if (mode == MODE_EDIT) {
                mode = MODE_VIEW;
                populateFromEntity(currentReport);
                applyMode();
            } else {
                finish();
            }
        });

        binding.btnSaveReport.setOnClickListener(v -> {
            if (mode == MODE_CREATE) saveNewReport();
            else if (mode == MODE_EDIT) updateReport();
        });

        binding.btnEditReport.setOnClickListener(v -> {
            mode = MODE_EDIT;
            applyMode();
        });

        binding.btnTakePhoto.setOnClickListener(v -> checkCameraPermission());
    }

    // ── Chargement d'un rapport existant ─────────────────────────────────────

    private void loadReport(int reportId) {
        viewModel.getById(reportId).observe(this, report -> {
            if (report != null && currentReport == null) {
                currentReport = report;
                populateFromEntity(report);
                applyMode();
                // Masquer le bouton Modifier si le citoyen connecté n'est pas l'auteur
                long connectedUserId = SessionManager.getUserId(this);
                if (report.citizenId != connectedUserId) {
                    binding.btnEditReport.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * Remplit les champs du formulaire à partir d'un ReportEntity existant.
     */
    private void populateFromEntity(ReportEntity report) {
        dto.title.set(report.title != null ? report.title : "");
        dto.description.set(report.description != null ? report.description : "");

        if (report.category != null) {
            binding.spCategory.setSelection(report.category.ordinal() + 1);
        }

        photoUris.clear();
        if (report.photoUri != null && !report.photoUri.isEmpty()) {
            photoUris.addAll(Arrays.asList(report.photoUri.split("\\|")));
        }
        refreshPhotoStrip();
    }

    // ── Mise à jour de l'interface selon le mode ──────────────────────────────

    private void applyMode() {
        boolean editable = (mode != MODE_VIEW);

        binding.etTitle.setEnabled(editable);
        binding.etDescription.setEnabled(editable);
        binding.spCategory.setEnabled(editable);

        binding.btnTakePhoto.setVisibility(editable ? View.VISIBLE : View.GONE);
        binding.btnSaveReport.setVisibility(mode != MODE_VIEW ? View.VISIBLE : View.GONE);
        binding.btnEditReport.setVisibility(mode == MODE_VIEW ? View.VISIBLE : View.GONE);

        switch (mode) {
            case MODE_CREATE: binding.tvReportFormTitle.setText("Ajouter un rapport"); break;
            case MODE_VIEW:   binding.tvReportFormTitle.setText("Détails du rapport"); break;
            case MODE_EDIT:   binding.tvReportFormTitle.setText("Modifier le rapport"); break;
        }

        refreshPhotoStrip();
    }

    // ── Sauvegarde / Mise à jour ──────────────────────────────────────────────

    private void saveNewReport() {
        if (!validateFields()) return;

        int pos = binding.spCategory.getSelectedItemPosition();
        dto.category  = ReportCategory.values()[pos - 1];
        dto.photoUris = new ArrayList<>(photoUris);
        dto.citizenId = (int) SessionManager.getUserId(this);
        dto.latitude  = 0.0;
        dto.longitude = 0.0;

        // ReportRepository.insert() exécute la requête via son ExecutorService interne.
        // L'appel est non-bloquant : on peut afficher le Toast et fermer l'activité
        // immédiatement sur le thread principal.
        viewModel.insert(dto);
        Toast.makeText(this, "Rapport enregistré !", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void updateReport() {
        if (!validateFields()) return;

        int pos = binding.spCategory.getSelectedItemPosition();
        if (pos == 0) { toast("Veuillez choisir une catégorie"); return; }

        currentReport.title       = dto.title.get();
        currentReport.description = dto.description.get();
        currentReport.category    = ReportCategory.values()[pos - 1];
        currentReport.photoUri    = photoUris.isEmpty() ? null : String.join("|", photoUris);

        // Vérification de propriété côté UI avant d'envoyer au ViewModel/Repository.
        long connectedUserId = SessionManager.getUserId(this);
        if (currentReport.citizenId != connectedUserId) {
            toast("Vous n'êtes pas autorisé à modifier ce rapport.");
            return;
        }
        viewModel.update(currentReport, connectedUserId);
        Toast.makeText(this, "Rapport mis à jour !", Toast.LENGTH_SHORT).show();
        mode = MODE_VIEW;
        applyMode();
    }

    private boolean validateFields() {
        String title = dto.title.get() != null ? dto.title.get().trim() : "";
        String desc  = dto.description.get() != null ? dto.description.get().trim() : "";

        if (title.isEmpty()) { toast("Veuillez saisir un titre"); return false; }
        if (desc.isEmpty())  { toast("Veuillez saisir une description"); return false; }

        int pos = binding.spCategory.getSelectedItemPosition();
        if (pos == 0) { toast("Veuillez choisir une catégorie"); return false; }

        return true;
    }

    // ── Gestion des photos ────────────────────────────────────────────────────

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        try {
            File photoFile = createImageFile();
            pendingPhotoUri = FileProvider.getUriForFile(
                    this, getPackageName() + ".fileprovider", photoFile
            );
            cameraLauncher.launch(pendingPhotoUri);
        } catch (Exception e) {
            toast("Erreur caméra : " + e.getMessage());
        }
    }

    private File createImageFile() throws IOException {
        String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File dir  = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile("REPORT_" + ts, ".jpg", dir);
    }

    /**
     * Reconstruit la bande de miniatures dans le HorizontalScrollView.
     *
     * En mode EDIT/CREATE : chaque miniature a un bouton "×" pour la supprimer.
     * En mode VIEW : les photos sont affichées en lecture seule.
     */
    private void refreshPhotoStrip() {
        LinearLayout strip = binding.llPhotos;
        strip.removeAllViews();

        int size   = (int) (110 * getResources().getDisplayMetrics().density);
        int margin = (int) (8   * getResources().getDisplayMetrics().density);
        boolean editable = (mode != MODE_VIEW);

        for (int i = 0; i < photoUris.size(); i++) {
            final int idx = i;
            String uriStr = photoUris.get(i);

            android.widget.RelativeLayout container = new android.widget.RelativeLayout(this);
            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(size, size);
            containerParams.setMargins(0, 0, margin, 0);
            container.setLayoutParams(containerParams);

            ImageView iv = new ImageView(this);
            android.widget.RelativeLayout.LayoutParams ivParams =
                    new android.widget.RelativeLayout.LayoutParams(
                            android.widget.RelativeLayout.LayoutParams.MATCH_PARENT,
                            android.widget.RelativeLayout.LayoutParams.MATCH_PARENT);
            iv.setLayoutParams(ivParams);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            try { iv.setImageURI(Uri.parse(uriStr)); }
            catch (Exception ignored) { iv.setBackgroundColor(Color.LTGRAY); }
            container.addView(iv);

            if (editable) {
                android.widget.TextView btnRemove = new android.widget.TextView(this);
                int btnSize = (int) (24 * getResources().getDisplayMetrics().density);
                android.widget.RelativeLayout.LayoutParams btnParams =
                        new android.widget.RelativeLayout.LayoutParams(btnSize, btnSize);
                btnParams.addRule(android.widget.RelativeLayout.ALIGN_PARENT_TOP);
                btnParams.addRule(android.widget.RelativeLayout.ALIGN_PARENT_END);
                btnRemove.setLayoutParams(btnParams);
                btnRemove.setText("✕");
                btnRemove.setTextSize(12f);
                btnRemove.setGravity(Gravity.CENTER);
                btnRemove.setBackgroundColor(0xBB000000);
                btnRemove.setTextColor(Color.WHITE);
                btnRemove.setOnClickListener(v -> {
                    photoUris.remove(idx);
                    refreshPhotoStrip();
                });
                container.addView(btnRemove);
            }

            strip.addView(container);
        }

        if (photoUris.isEmpty()) {
            android.widget.TextView empty = new android.widget.TextView(this);
            empty.setText("Aucune photo");
            empty.setTextColor(Color.GRAY);
            empty.setGravity(Gravity.CENTER_VERTICAL);
            strip.addView(empty);
        }
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
