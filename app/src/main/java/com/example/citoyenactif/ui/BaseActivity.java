package com.example.citoyenactif.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Map;

/**
 * Activité de base du projet CitoyenActif.
 *
 * Centralise la gestion de toutes les permissions runtime de l'application.
 * Toutes les activités du projet doivent étendre cette classe au lieu d'AppCompatActivity.
 *
 * Permissions gérées :
 *   - CAMERA               : prise de photo pour les rapports / preuves
 *   - ACCESS_FINE_LOCATION : géolocalisation précise (GPS)
 *   - ACCESS_COARSE_LOCATION : géolocalisation approximative (réseau)
 *   - READ_MEDIA_IMAGES    : lecture des images (Android 13+)
 *   - READ_EXTERNAL_STORAGE: lecture du stockage (Android ≤ 12)
 *
 * Utilisation dans une sous-classe :
 * <pre>
 *   // Vérifier
 *   if (hasCameraPermission()) { ... }
 *
 *   // Demander
 *   requestCameraPermission();
 *
 *   // Réagir au résultat
 *   {@literal @}Override
 *   protected void onPermissionsResult(Map<String, Boolean> results) {
 *       if (Boolean.TRUE.equals(results.get(PERM_CAMERA))) { openCamera(); }
 *   }
 * </pre>
 */
public abstract class BaseActivity extends AppCompatActivity {

    // ── Constantes de permissions ─────────────────────────────────────────────

    /** Permission caméra — capture de photos pour les rapports et les preuves. */
    public static final String PERM_CAMERA = Manifest.permission.CAMERA;

    /** Géolocalisation précise (GPS) — requise pour positionner un rapport sur la carte. */
    public static final String PERM_LOCATION_FINE = Manifest.permission.ACCESS_FINE_LOCATION;

    /** Géolocalisation approximative (réseau Wi-Fi/cellulaire). */
    public static final String PERM_LOCATION_COARSE = Manifest.permission.ACCESS_COARSE_LOCATION;

    /**
     * Lecture des images de la galerie.
     * Android 13+ (API 33) : READ_MEDIA_IMAGES
     * Android 12 et moins  : READ_EXTERNAL_STORAGE
     */
    public static final String PERM_READ_IMAGES =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    ? Manifest.permission.READ_MEDIA_IMAGES
                    : Manifest.permission.READ_EXTERNAL_STORAGE;

    /** Écriture sur le stockage externe — requis uniquement pour Android ≤ 9 (API 28). */
    public static final String PERM_WRITE_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    // ── Launcher (une seule instance, réutilisable pour toutes les permissions) ─

    /**
     * Launcher pour demander une ou plusieurs permissions simultanément.
     * Le résultat est transmis à {@link #onPermissionsResult(Map)}.
     */
    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    this::onPermissionsResult
            );

    // ── API publique — vérifications ──────────────────────────────────────────

    /**
     * Vérifie si une permission spécifique est accordée.
     *
     * @param permission constante Manifest.permission.*
     * @return true si accordée, false sinon
     */
    public boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    /** @return true si la permission CAMERA est accordée. */
    public boolean hasCameraPermission() {
        return hasPermission(PERM_CAMERA);
    }

    /**
     * @return true si au moins ACCESS_FINE_LOCATION ou ACCESS_COARSE_LOCATION est accordée.
     */
    public boolean hasLocationPermission() {
        return hasPermission(PERM_LOCATION_FINE) || hasPermission(PERM_LOCATION_COARSE);
    }

    /** @return true si la permission de lecture des images est accordée. */
    public boolean hasStoragePermission() {
        return hasPermission(PERM_READ_IMAGES);
    }

    // ── API publique — demandes ───────────────────────────────────────────────

    /**
     * Demande une ou plusieurs permissions à l'utilisateur.
     * Le résultat sera transmis à {@link #onPermissionsResult(Map)}.
     *
     * @param permissions une ou plusieurs constantes Manifest.permission.*
     */
    public void requestPermissions(String... permissions) {
        permissionLauncher.launch(permissions);
    }

    /** Demande uniquement la permission CAMERA. */
    public void requestCameraPermission() {
        requestPermissions(PERM_CAMERA);
    }

    /** Demande ACCESS_FINE_LOCATION et ACCESS_COARSE_LOCATION ensemble. */
    public void requestLocationPermissions() {
        requestPermissions(PERM_LOCATION_FINE, PERM_LOCATION_COARSE);
    }

    /** Demande la permission de lecture des images de la galerie. */
    public void requestStoragePermission() {
        requestPermissions(PERM_READ_IMAGES);
    }

    /**
     * Demande toutes les permissions de l'application en une seule boîte de dialogue.
     * À appeler au lancement pour éviter les demandes éparses.
     */
    public void requestAllPermissions() {
        requestPermissions(
                PERM_CAMERA,
                PERM_LOCATION_FINE,
                PERM_LOCATION_COARSE,
                PERM_READ_IMAGES
        );
    }

    // ── Callback — à surcharger dans les sous-classes ─────────────────────────

    /**
     * Appelé automatiquement après que l'utilisateur a répondu à une demande de permissions.
     *
     * À surcharger dans une sous-classe pour réagir au résultat :
     * <pre>
     *   {@literal @}Override
     *   protected void onPermissionsResult(Map<String, Boolean> results) {
     *       if (Boolean.TRUE.equals(results.get(PERM_CAMERA))) {
     *           openCamera(); // permission accordée
     *       } else {
     *           Toast.makeText(this, "Permission caméra refusée", Toast.LENGTH_SHORT).show();
     *       }
     *   }
     * </pre>
     *
     * @param results clé = nom de la permission, valeur = true si accordée
     */
    protected void onPermissionsResult(Map<String, Boolean> results) {
        // Implémentation par défaut vide — à surcharger si nécessaire
    }
}
