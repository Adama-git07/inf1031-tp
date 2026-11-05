package com.example.citoyenactif.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.function.Consumer;

/**
 * Utilitaire pour récupérer la position GPS de l'utilisateur.
 *
 * Utilise FusedLocationProviderClient (Google Play Services)
 * pour un accès simplifié et optimisé à la localisation.
 */
public class LocationUtils {

    /**
     * Vérifie si la permission de localisation précise est accordée.
     *
     * @param context contexte Android
     * @return true si ACCESS_FINE_LOCATION est accordée
     */
    public static boolean hasLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Récupère la dernière position connue de l'appareil (non bloquant).
     *
     * @param context  contexte Android
     * @param callback appelé avec la Location (ou null si indisponible)
     */
    public static void getLastLocation(Context context, Consumer<Location> callback) {
        if (!hasLocationPermission(context)) {
            callback.accept(null);
            return;
        }

        FusedLocationProviderClient client =
                LocationServices.getFusedLocationProviderClient(context);

        try {
            client.getLastLocation()
                    .addOnSuccessListener(callback::accept)
                    .addOnFailureListener(e -> callback.accept(null));
        } catch (SecurityException e) {
            callback.accept(null);
        }
    }

    /**
     * Récupère la position actuelle de l'appareil (plus précis que getLastLocation).
     *
     * @param context  contexte Android
     * @param callback appelé avec la Location (ou null si indisponible)
     */
    public static void getCurrentLocation(Context context, Consumer<Location> callback) {
        if (!hasLocationPermission(context)) {
            callback.accept(null);
            return;
        }

        FusedLocationProviderClient client =
                LocationServices.getFusedLocationProviderClient(context);

        try {
            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(callback::accept)
                    .addOnFailureListener(e -> callback.accept(null));
        } catch (SecurityException e) {
            callback.accept(null);
        }
    }
}