package com.example.citoyenactif.ui.viewmodel;

import android.Manifest;
import android.app.Application;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.LatLng;

/**
 * ViewModel de localisation — partagé entre toutes les activités et fragments.
 *
 * Responsabilités :
 *   1. {@link #locate()} — obtient la position GPS actuelle en une seule lecture
 *      (s'arrête dès la première valeur reçue) et la poste dans {@link #getCurrentLocation()}.
 *   2. {@link #setSelectedMapLocation(LatLng)} — mémorise la position sélectionnée
 *      via un clic sur la carte. Observée par ReportActivity pour pré-remplir le formulaire.
 *
 * Utilisé par :
 *   - BaseActivity (instance unique par activité, accessible via getLocationViewModel())
 *   - ReportActivity (observe currentLocation pour afficher lat/lng)
 *   - ReportMapFragment (appelle setSelectedMapLocation au clic carte, observe pour créer rapport)
 */
public class LocationViewModel extends AndroidViewModel {

    private final FusedLocationProviderClient fusedLocationClient;

    /** Position GPS actuelle (one-shot, mise à jour à chaque appel à locate()). */
    private final MutableLiveData<Location> currentLocation = new MutableLiveData<>();

    /**
     * Position sélectionnée par un clic sur la carte Google Maps.
     * Permet à ReportMapFragment de transmettre un emplacement à ReportActivity
     * sans passer par les extras d'Intent (architecture réactive).
     */
    private final MutableLiveData<LatLng> selectedMapLocation = new MutableLiveData<>();

    public LocationViewModel(@NonNull Application application) {
        super(application);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(application);
    }

    // ── Accesseurs LiveData ───────────────────────────────────────────────────

    /** Position GPS courante — observée pour mettre à jour les coordonnées d'un rapport. */
    public LiveData<Location> getCurrentLocation() {
        return currentLocation;
    }

    /**
     * Position sélectionnée sur la carte.
     * Observée par ReportActivity pour pré-remplir lat/lng quand on vient de la carte.
     */
    public LiveData<LatLng> getSelectedMapLocation() {
        return selectedMapLocation;
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    /**
     * Déclenche une lecture one-shot de la position GPS.
     *
     * Utilise LocationRequest haute précision (intervalle 500 ms).
     * Se désenregistre automatiquement dès la première valeur reçue.
     * Poste le résultat dans {@link #getCurrentLocation()}.
     *
     * Précondition : les permissions ACCESS_FINE_LOCATION et ACCESS_COARSE_LOCATION
     * doivent avoir été accordées avant d'appeler cette méthode.
     */
    @RequiresPermission(allOf = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    })
    public void locate() {
        LocationRequest request = new LocationRequest.Builder(500L)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMinUpdateDistanceMeters(1.0f)
                .build();

        LocationCallback callback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location last = locationResult.getLastLocation();
                if (last == null) return;

                // Poster la position sur le thread principal
                currentLocation.postValue(last);

                // One-shot : on se désenregistre immédiatement
                fusedLocationClient.removeLocationUpdates(this);
            }
        };

        fusedLocationClient.requestLocationUpdates(request, callback, Looper.getMainLooper());
    }

    /**
     * Mémorise la position cliquée sur la carte Google Maps.
     *
     * Appelée par ReportMapFragment quand l'utilisateur touche la carte.
     * ReportActivity observe ce LiveData pour pré-remplir les coordonnées du formulaire.
     *
     * @param latLng coordonnées du point touché
     */
    public void setSelectedMapLocation(LatLng latLng) {
        selectedMapLocation.setValue(latLng);
    }

    /**
     * Réinitialise la position sélectionnée (après que ReportActivity l'a consommée).
     */
    public void clearSelectedMapLocation() {
        selectedMapLocation.setValue(null);
    }
}
