package com.example.citoyenactif.ui.report;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.citoyenactif.R;
import com.example.citoyenactif.data.local.entity.ReportEntity;
import com.example.citoyenactif.data.repository.ReportRepository;
import com.example.citoyenactif.databinding.ReportActivityBinding;
import com.example.citoyenactif.model.enums.ReportCategory;
import com.example.citoyenactif.model.enums.ReportStatus;

/**
 * Activité permettant d'ajouter un rapport de bris.
 *
 * Récupère les informations du formulaire, valide les champs,
 * puis enregistre le rapport dans Room.
 */
public class ReportActivity extends AppCompatActivity {

    private ReportActivityBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // DataBinding : report_activity.xml
        binding = DataBindingUtil.setContentView(this, R.layout.report_activity);

        binding.btnCancelReport.setOnClickListener(v -> finish());
        binding.btnSaveReport.setOnClickListener(v -> saveReport());
    }

    /**
     * Valide le formulaire puis enregistre le rapport dans Room
     * dans un thread secondaire.
     */
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

        // Spinner : si tu as mis "Choisir une catégorie..." en premier
        int pos = binding.spCategory.getSelectedItemPosition();
        if (pos == 0) {
            Toast.makeText(this, "Veuillez choisir une catégorie", Toast.LENGTH_SHORT).show();
            return;
        }

        // IMPORTANT : pour éviter les espaces/accents, on mappe l'index du spinner vers l'enum
        // Exemple attendu : items du spinner dans le même ordre que l'enum (VOIRIE, ECLAIRAGE, PARC, AQUEDUC, AUTRE)
        final ReportCategory category = ReportCategory.values()[pos - 1];

        ReportEntity report = new ReportEntity();
        report.title = title;
        report.description = description;
        report.category = category;
        report.status = ReportStatus.EN_ATTENTE; // statut initial

        // Valeurs par défaut (tu remplaceras plus tard)
        report.latitude = 0.0;
        report.longitude = 0.0;
        report.photoPath = null;

        report.createdAt = System.currentTimeMillis();

        // TODO : récupérer le vrai userId connecté (session)
        report.createdByUserId = 0;

        new Thread(() -> {
            try {
                ReportRepository repo = new ReportRepository(getApplicationContext());
                long id = repo.addReport(report);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Rapport enregistré (id=" + id + ")", Toast.LENGTH_SHORT).show();
                    finish();
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Erreur lors de l'enregistrement", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
}