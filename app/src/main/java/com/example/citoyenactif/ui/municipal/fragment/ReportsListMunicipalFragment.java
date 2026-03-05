package com.example.citoyenactif.ui.municipal.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.citoyenactif.data.local.entity.ReportEntity;
import com.example.citoyenactif.data.repository.ReportRepository;
import com.example.citoyenactif.databinding.ReportsListMunicipalFragmentBinding;
import com.example.citoyenactif.model.enums.ReportStatus;
import com.example.citoyenactif.ui.municipal.adapter.ReportAdapter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Fragment de la partie municipale affichant la liste des signalements.
 *
 * Responsabilités :
 * - Afficher les signalements dans un RecyclerView via ReportAdapter
 * - Observer les données (LiveData) pour mettre la liste à jour automatiquement
 * - Permettre à l'agent de changer le statut d'un signalement
 * - Si l'agent choisit le statut RESOLU : obliger la prise d'une photo (preuve)
 *
 * Notes techniques :
 * - Utilise ReportRepository pour interagir avec la base de données Room
 * - Utilise ActivityResultLauncher pour gérer la caméra (TakePicture)
 * - Utilise FileProvider pour obtenir une URI sécurisée d'écriture du fichier photo
 */
public class ReportsListMunicipalFragment extends Fragment {

    /** Binding du layout reports_list_municipal_fragment.xml */
    private ReportsListMunicipalFragmentBinding binder;

    /** Repository d'accès aux signalements (Room) */
    private ReportRepository repo;

    /** Adapter du RecyclerView pour afficher les signalements */
    private ReportAdapter adapter;

    // ============================
    // Flow caméra (preuve RESOLU)
    // ============================

    /**
     * Launcher caméra (TakePicture) : enregistre la photo dans pendingPhotoUri
     * puis exécute un callback success/fail.
     */
    private ActivityResultLauncher<Uri> takePictureLauncher;

    /** URI temporaire du fichier photo à écrire (préparée avant d'ouvrir la caméra) */
    private Uri pendingPhotoUri;

    /** Signalement dont on veut marquer le statut RESOLU (en attente de preuve photo) */
    private ReportEntity pendingResolveReport;

    /**
     * Crée la vue du fragment et initialise :
     * - binding
     * - repository
     * - RecyclerView + adapter
     * - observation LiveData (mise à jour automatique)
     * - launcher caméra (résolution avec preuve photo)
     *
     * @param inflater inflater utilisé pour créer la vue
     * @param container conteneur parent (peut être null)
     * @param savedInstanceState état sauvegardé (si rotation / recreation)
     * @return la racine de la vue du fragment
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        // Initialise le binding (ViewBinding généré)
        binder = ReportsListMunicipalFragmentBinding.inflate(inflater, container, false);

        // Initialise le repository (constructeur basé sur Application)
        repo = new ReportRepository(requireActivity().getApplication());

        // ============================
        // RecyclerView + Adapter
        // ============================

        // L'adapter reçoit un callback : quand on clique, on ouvre un dialog de statut
        adapter = new ReportAdapter(this::showStatusDialog);

        // LayoutManager vertical
        binder.rvReports.setLayoutManager(new LinearLayoutManager(getContext()));
        binder.rvReports.setAdapter(adapter);

        /**
         * Observation LiveData :
         * - Chaque fois que la liste de signalements change dans Room,
         *   l'adapter est automatiquement mis à jour.
         */
        repo.getAllReports().observe(getViewLifecycleOwner(),
                reports -> adapter.submit(reports));

        // ============================
        // Launcher caméra (preuve)
        // ============================

        /**
         * Callback caméra :
         * - Si success = true et qu'on a un report en attente + une URI valide,
         *   alors on marque le report "RESOLU" et on enregistre la preuve photo.
         * - Sinon : photo annulée → statut non modifié.
         */
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {

                    if (Boolean.TRUE.equals(success)
                            && pendingResolveReport != null
                            && pendingPhotoUri != null) {

                        // Mise à jour DB : statut RESOLU + preuve
                        repo.resolveWithProof(
                                pendingResolveReport.id,
                                pendingPhotoUri.toString()
                        );

                        Toast.makeText(getContext(),
                                "Report marqué RESOLU (preuve enregistrée)",
                                Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(getContext(),
                                "Photo annulée : statut non modifié",
                                Toast.LENGTH_SHORT).show();
                    }

                    // Nettoyage des variables temporaires
                    pendingResolveReport = null;
                    pendingPhotoUri = null;
                }
        );

        // Retourne la vue racine du fragment
        return binder.getRoot();
    }

    /**
     * Affiche un dialog permettant à l'agent de choisir un statut pour un signalement.
     *
     * Logique :
     * - Si le statut choisi est RESOLU :
     *      -> on force la prise d'une photo (preuve) via la caméra
     * - Sinon :
     *      -> simple mise à jour du statut dans la BD
     *
     * @param report signalement sélectionné dans la liste
     */
    private void showStatusDialog(ReportEntity report) {
        if (report == null) return;

        // Récupère tous les statuts possibles
        ReportStatus[] statuses = ReportStatus.values();

        // Labels affichés dans le dialog (nom des enums)
        String[] labels = new String[statuses.length];

        // Statut actuellement coché (pour pré-sélection)
        int checked = -1;

        for (int i = 0; i < statuses.length; i++) {
            labels[i] = statuses[i].name();

            // Compare le statut actuel du report au statut de la liste
            if (report.status == statuses[i]) checked = i;
        }

        // Construction du dialog
        new AlertDialog.Builder(requireContext())
                .setTitle("Changer le statut")
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {

                    // Statut choisi par l'utilisateur
                    ReportStatus chosen = statuses[which];

                    // ============================
                    // Cas spécial : RESOLU
                    // ============================
                    if (chosen == ReportStatus.RESOLU) {

                        // On garde le report en mémoire (en attente de la photo)
                        pendingResolveReport = report;

                        // On prépare une URI de fichier où la caméra va écrire l'image
                        pendingPhotoUri = createImageUriInFiles();

                        // Si on n'arrive pas à préparer l'URI, on annule
                        if (pendingPhotoUri == null) {
                            Toast.makeText(getContext(),
                                    "Impossible de préparer la caméra",
                                    Toast.LENGTH_SHORT).show();

                            pendingResolveReport = null;
                            dialog.dismiss();
                            return;
                        }

                        // Ouvre la caméra et écrit dans pendingPhotoUri
                        takePictureLauncher.launch(pendingPhotoUri);

                        dialog.dismiss();
                        return;
                    }

                    // ============================
                    // Autres statuts : update simple
                    // ============================
                    repo.updateStatus(report.id, chosen.name());

                    Toast.makeText(getContext(),
                            "Statut mis à jour : " + chosen.name(),
                            Toast.LENGTH_SHORT).show();

                    dialog.dismiss();
                })
                .setNegativeButton("Annuler", (d, w) -> d.dismiss())
                .show();
    }

    /**
     * Crée une URI via FileProvider pour enregistrer une photo dans :
     * /data/data/<package>/files/images/
     *
     * Important :
     * - Compatible avec file_paths.xml si tu as :
     *   <files-path name="images" path="images/" />
     *
     * @return Uri sécurisée (FileProvider) ou null si échec
     */
    private Uri createImageUriInFiles() {
        try {
            // Dossier interne "files/images"
            File dir = new File(requireContext().getFilesDir(), "images");

            // Création du dossier s'il n'existe pas
            if (!dir.exists()) dir.mkdirs();

            // Nom unique du fichier
            String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

            // Création du fichier temporaire image
            File image = File.createTempFile("RESOLVE_" + ts + "_", ".jpg", dir);

            // Retourne l'URI FileProvider sécurisée
            return FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    image
            );

        } catch (IOException e) {
            // Erreur lors de la création du fichier
            return null;
        }
    }

    /**
     * Nettoie le binding lorsque la vue est détruite.
     *
     * Bonne pratique :
     * évite les fuites mémoire (memory leaks) dans les Fragments.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binder = null;
    }
}