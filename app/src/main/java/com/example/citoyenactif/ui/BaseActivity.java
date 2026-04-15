package com.example.citoyenactif.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.citoyenactif.ui.viewmodel.LocationViewModel;

import java.util.Map;

/**
 * Activité de base du projet CitoyenActif.
 *
 * Centralise deux responsabilités transversales :
 *
 * 1. Permissions runtime — toutes les constantes et helpers de demande/vérification
 *    de permissions (caméra, localisation, stockage). Les sous-classes surchargent
 *    {@link #onPermissionsResult(Map)} pour réagir aux résultats.
 *
 * 2. LocationViewModel — instance unique par activité (survit aux rotations).
 *    Accessible via {@link #getLocationViewModel()} depuis n'importe quelle
 *    sous-classe ou fragment attaché. Fournit :
 *      - locate()                : obtient la position GPS (one-shot)
 *      - currentLocation         : LiveData<Location> observable
 *      - selectedMapLocation     : LiveData<LatLng> positionné par un clic carte
 *
 * Toutes les activités du projet étendent cette classe.
 */
public abstract class BaseActivity extends AppCompatActivity {

    // ══════════════════════════════════════════════════════════════════════════
    //  PERMISSIONS — Constantes
    // ══════════════════════════════════════════════════════════════════════════

    /** Permission caméra — capture de photos pour les rapports et les preuves. */
    public static final String PERM_CAMERA = Manifest.permission.CAMERA;

    /** Géolocalisation précise (GPS). */
    public static final String PERM_LOCATION_FINE = Manifest.permission.ACCESS_FINE_LOCATION;

    /** Géolocalisation approximative (réseau Wi-Fi/cellulaire). */
    public static final String PERM_LOCATION_COARSE = Manifest.permission.ACCESS_COARSE_LOCATION;

    /**
     * Lecture des images de la galerie.
     * Android 13+ (API 33) : READ_MEDIA_IMAGES
     * Android ≤ 12         : READ_EXTERNAL_STORAGE
     */
    public static final String PERM_READ_IMAGES =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    ? Manifest.permission.READ_MEDIA_IMAGES
                    : Manifest.permission.READ_EXTERNAL_STORAGE;

    /** Écriture sur le stockage externe — requis uniquement pour Android ≤ 9 (API 28). */
    public static final String PERM_WRITE_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    // ══════════════════════════════════════════════════════════════════════════
    //  PERMISSIONS — Launcher
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Launcher pour demander une ou plusieurs permissions simultanément.
     * Le résultat est transmis à {@link #onPermissionsResult(Map)}.
     */
    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    this::onPermissionsResult
            );

    // ══════════════════════════════════════════════════════════════════════════
    //  LOCATION VIEWMODEL
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * ViewModel de localisation — instance unique par activité.
     * Survit aux rotations d'écran grâce à ViewModelProvider.
     * Accessible depuis les sous-classes via {@link #getLocationViewModel()}.
     */
    private LocationViewModel locationViewModel;

    // ══════════════════════════════════════════════════════════════════════════
    //  LIFECYCLE
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialiser le LocationViewModel une seule fois par activité
        locationViewModel = new ViewModelProvider(this).get(LocationViewModel.class);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  LOCATION — Accesseur
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Retourne le LocationViewModel de cette activité.
     *
     * Utilisations typiques :
     * <pre>
     *   // Déclencher la localisation GPS
     *   getLocationViewModel().locate();
     *
     *   // Observer la position courante
     *   getLocationViewModel().getCurrentLocation().observe(this, loc -> { ... });
     *
     *   // Observer la position sélectionnée sur la carte
     *   getLocationViewModel().getSelectedMapLocation().observe(this, latLng -> { ... });
     * </pre>
     */
    public LocationViewModel getLocationViewModel() {
        return locationViewModel;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PERMISSIONS — Vérifications
    // ══════════════════════════════════════════════════════════════════════════

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

    /** @return true si ACCESS_FINE_LOCATION ou ACCESS_COARSE_LOCATION est accordée. */
    public boolean hasLocationPermission() {
        return hasPermission(PERM_LOCATION_FINE) || hasPermission(PERM_LOCATION_COARSE);
    }

    /** @return true si la permission de lecture des images est accordée. */
    public boolean hasStoragePermission() {
        return hasPermission(PERM_READ_IMAGES);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PERMISSIONS — Demandes
    // ══════════════════════════════════════════════════════════════════════════

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

    /** Demande la permission de lecture des images. */
    public void requestStoragePermission() {
        requestPermissions(PERM_READ_IMAGES);
    }

    /**
     * Demande toutes les permissions de l'application en une seule boîte de dialogue.
     * À appeler au premier lancement pour éviter les demandes éparses.
     */
    public void requestAllPermissions() {
        requestPermissions(
                PERM_CAMERA,
                PERM_LOCATION_FINE,
                PERM_LOCATION_COARSE,
                PERM_READ_IMAGES
        );
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PERMISSIONS — Callback (à surcharger)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Appelé automatiquement après la réponse de l'utilisateur aux demandes de permissions.
     *
     * À surcharger dans une sous-classe pour réagir :
     * <pre>
     *   {@literal @}Override
     *   protected void onPermissionsResult(Map<String, Boolean> results) {
     *       if (Boolean.TRUE.equals(results.get(PERM_CAMERA))) {
     *           openCamera();
     *       }
     *       if (Boolean.TRUE.equals(results.get(PERM_LOCATION_FINE))) {
     *           getLocationViewModel().locate();
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
