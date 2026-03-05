package com.example.citoyenactif.ui.report;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.citoyenactif.R;
import com.example.citoyenactif.data.repository.ReportRepository;
import com.example.citoyenactif.databinding.ReportActivityBinding;
import com.example.citoyenactif.model.dto.ReportCreateDTO;
import com.example.citoyenactif.model.enums.ReportCategory;

/**
 * Activité permettant d'ajouter un rapport de bris.
 * Récupère les informations du formulaire, valide les champs,
 * puis enregistre le rapport dans Room via ReportRepository.
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
     * dans un thread secondaire via le DTO et le Repository.
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

        // Spinner : "Choisir une catégorie..." est à l'index 0
        int pos = binding.spCategory.getSelectedItemPosition();
        if (pos == 0) {
            Toast.makeText(this, "Veuillez choisir une catégorie", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mappage index spinner → enum (ordre : VOIRIE, ECLAIRAGE, PARC, AQUEDUC, AUTRE)
        final ReportCategory category = ReportCategory.values()[pos - 1];

        // ✅ On passe par le DTO — jamais ReportEntity directement ici
        ReportCreateDTO dto = new ReportCreateDTO();
        dto.title       = title;
        dto.description = description;
        dto.category    = category;
        dto.photoUri    = null;  // TODO : photo avec caméra
        dto.latitude    = 0.0;  // TODO : GPS réel
        dto.longitude   = 0.0;
        dto.citizenId   = 0;    // TODO : récupérer via SessionManager

        // ✅ Thread secondaire conservé
        new Thread(() -> {
            try {
                // ✅ getApplication() (Application), pas getApplicationContext() (Context)
                ReportRepository repo = new ReportRepository(getApplication());

                // ✅ insert(dto) — repo gère la conversion DTO → Entity
                repo.insert(dto);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Rapport enregistré !", Toast.LENGTH_SHORT).show();
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