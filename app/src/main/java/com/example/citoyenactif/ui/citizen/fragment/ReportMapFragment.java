package com.example.citoyenactif.ui.citizen.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.citoyenactif.R;
import com.example.citoyenactif.data.local.entity.ReportEntity;
import com.example.citoyenactif.databinding.ReportsMapFragmentBinding;
import com.example.citoyenactif.model.enums.ReportStatus;
import com.example.citoyenactif.ui.BaseActivity;
import com.example.citoyenactif.ui.report.ReportActivity;
import com.example.citoyenactif.ui.viewmodel.ReportViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

/**
 * Fragment "Carte des rapports" — Google Maps SDK for Android.
 *
 * Couleurs des marqueurs selon le statut :
 *   - EN_ATTENTE  → rouge
 *   - EN_COURS    → orange
 *   - RESOLU      → vert
 *   - IGNORE      → azure (bleu transparent)
 *
 * Position utilisateur :
 *   - Petit cercle violet style Google Maps
 *   - Sauvegardée dans lastUserLocation pour être redessinée après clear()
 *
 * Nouveau rapport :
 *   - Pin rouge de sélection + petit cercle violet sur la position cliquée
 */
public class ReportMapFragment extends Fragment implements OnMapReadyCallback {

    /** Tag spécial sur le marqueur de sélection. */
    private static final String TAG_NEW_REPORT = "new_report";

    private ReportsMapFragmentBinding binder;
    private GoogleMap googleMap;
    private ReportViewModel viewModel;

    /** Dernière position GPS connue — conservée pour redessiner après clear(). */
    @Nullable private LatLng lastUserLocation = null;

    /** Marqueur + cercle de sélection (clic carte) — un seul à la fois. */
    @Nullable private Marker selectionMarker = null;
    @Nullable private Circle selectionCircle = null;

    /** Cercle + marqueur de position utilisateur. */
    @Nullable private Circle userLocationCircle = null;
    @Nullable private Marker userLocationMarker = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binder = ReportsMapFragmentBinding.inflate(inflater, container, false);
        return binder.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ── Insérer le SupportMapFragment ─────────────────────────────────────
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.mapContainer, mapFragment)
                .commit();

        mapFragment.getMapAsync(this);

        // ── ViewModel rapports ────────────────────────────────────────────────
        viewModel = new ViewModelProvider(this).get(ReportViewModel.class);

        // ── FAB → créer un rapport sans coordonnées ──────────────────────────
        binder.fabAddReport.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ReportActivity.class))
        );
    }

    // ─── OnMapReadyCallback ───────────────────────────────────────────────────

    @Override
    @SuppressLint("MissingPermission")
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;

        // ── Configuration de la carte ─────────────────────────────────────────
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setMapToolbarEnabled(true);

        // Désactiver le bouton Ma position natif (on gère nous-mêmes)
        map.setMyLocationEnabled(false);

        // Centre par défaut : Trois-Rivières
        LatLng troisRivieres = new LatLng(46.3498, -72.5529);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(troisRivieres, 13f));

        // Centrage + cercle violet sur position GPS
        centerOnUserLocation();

        // ── Marqueurs des rapports existants ──────────────────────────────────
        viewModel.getAllReports().observe(getViewLifecycleOwner(), this::refreshMarkers);

        // ── Clic carte vide → marqueur de sélection ───────────────────────────
        map.setOnMapClickListener(this::onMapClick);

        // ── Clic info-window ──────────────────────────────────────────────────
        map.setOnInfoWindowClickListener(this::onInfoWindowClick);
    }

    // ─── Position utilisateur ─────────────────────────────────────────────────

    /**
     * Centre la carte sur la position GPS et affiche un cercle violet.
     * Sauvegarde la position dans lastUserLocation pour la redessiner après clear().
     */
    @SuppressLint("MissingPermission")
    private void centerOnUserLocation() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) return;

        com.google.android.gms.location.FusedLocationProviderClient client =
                com.google.android.gms.location.LocationServices
                        .getFusedLocationProviderClient(requireContext());

        try {
            client.getLastLocation().addOnSuccessListener(location -> {
                if (location != null && googleMap != null) {
                    // Sauvegarder pour redessiner après clear()
                    lastUserLocation = new LatLng(
                            location.getLatitude(),
                            location.getLongitude()
                    );
                    requireActivity().runOnUiThread(() -> {
                        googleMap.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(lastUserLocation, 14f)
                        );
                        drawUserLocationCircle(lastUserLocation);
                    });
                }
            });
        } catch (SecurityException e) {
            // Garder le centre par défaut
        }
    }

    /**
     * Dessine un cercle violet style Google Maps sur la position utilisateur.
     * - Anneau violet semi-transparent
     * - Remplissage violet très transparent
     * - Point central (marqueur violet)
     *
     * @param position position GPS de l'utilisateur
     */
    private void drawUserLocationCircle(LatLng position) {
        // Supprimer l'ancien cercle si existant
        if (userLocationCircle != null) userLocationCircle.remove();
        if (userLocationMarker != null) userLocationMarker.remove();

        // Cercle extérieur violet semi-transparent
        userLocationCircle = googleMap.addCircle(new CircleOptions()
                .center(position)
                .radius(30)
                .strokeColor(Color.argb(200, 103, 58, 183))
                .strokeWidth(4f)
                .fillColor(Color.argb(40, 103, 58, 183))
        );

        // Point central violet
        userLocationMarker = googleMap.addMarker(new MarkerOptions()
                .position(position)
                .title("Ma position")
                .icon(BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_VIOLET))
                .anchor(0.5f, 0.5f)
                .zIndex(10f)
        );
    }

    // ─── Clic carte vide ─────────────────────────────────────────────────────

    /**
     * Place un pin rouge + petit cercle violet sur la position cliquée.
     * Le cercle indique visuellement l'emplacement du rapport à créer.
     *
     * @param latLng coordonnées du point touché
     */
    private void onMapClick(LatLng latLng) {
        // Supprimer anciens marqueurs de sélection
        if (selectionMarker != null) { selectionMarker.remove(); selectionMarker = null; }
        if (selectionCircle != null) { selectionCircle.remove(); selectionCircle = null; }

        // Pin rouge de sélection
        selectionMarker = googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Nouveau rapport ici")
                .snippet("Appuyez pour créer un rapport à cet emplacement.")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        );

        if (selectionMarker != null) {
            selectionMarker.setTag(TAG_NEW_REPORT);
            selectionMarker.showInfoWindow();
        }

        // Petit cercle violet autour du point sélectionné
        selectionCircle = googleMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(15)
                .strokeColor(Color.argb(200, 103, 58, 183))
                .strokeWidth(3f)
                .fillColor(Color.argb(40, 103, 58, 183))
        );

        // Mémoriser dans LocationViewModel
        if (requireActivity() instanceof BaseActivity) {
            ((BaseActivity) requireActivity())
                    .getLocationViewModel()
                    .setSelectedMapLocation(latLng);
        }
    }

    // ─── Clic info-window ─────────────────────────────────────────────────────

    /**
     * Traite le clic sur l'info-window d'un marqueur :
     *   - tag Integer   → rapport existant → mode VIEW
     *   - tag new_report → nouveau rapport → mode CREATE avec coordonnées
     */
    private void onInfoWindowClick(Marker marker) {
        Object tag = marker.getTag();

        if (tag instanceof Integer) {
            // Rapport existant → mode VIEW
            Intent intent = new Intent(requireContext(), ReportActivity.class);
            intent.putExtra(ReportActivity.EXTRA_REPORT_ID, (int) tag);
            startActivity(intent);

        } else if (TAG_NEW_REPORT.equals(tag)) {
            // Nouveau rapport → mode CREATE avec coordonnées
            LatLng pos = marker.getPosition();
            Intent intent = new Intent(requireContext(), ReportActivity.class);
            intent.putExtra(ReportActivity.EXTRA_LATITUDE,  pos.latitude);
            intent.putExtra(ReportActivity.EXTRA_LONGITUDE, pos.longitude);
            startActivity(intent);

            // Nettoyer après navigation
            selectionMarker.remove(); selectionMarker = null;
            if (selectionCircle != null) { selectionCircle.remove(); selectionCircle = null; }
        }
    }

    // ─── Marqueurs des rapports ───────────────────────────────────────────────

    /**
     * Efface tous les marqueurs et les recrée depuis Room.
     * Redessine immédiatement le cercle utilisateur depuis lastUserLocation
     * (sans appel GPS asynchrone) pour éviter le délai.
     *
     * Couleurs :
     *   - EN_ATTENTE → rouge
     *   - EN_COURS   → orange
     *   - RESOLU     → vert
     *   - IGNORE     → azure
     */
    private void refreshMarkers(@Nullable List<ReportEntity> reports) {
        if (googleMap == null) return;

        // Effacer tous les marqueurs
        googleMap.clear();
        selectionMarker = null;
        selectionCircle = null;
        userLocationCircle = null;
        userLocationMarker = null;

        // Redessiner immédiatement le cercle utilisateur si position déjà connue
        if (lastUserLocation != null) {
            drawUserLocationCircle(lastUserLocation);
        }

        if (reports == null) return;

        for (ReportEntity report : reports) {
            if (report.latitude == 0.0 && report.longitude == 0.0) continue;

            float hue     = getMarkerHue(report.status);
            String snippet = buildSnippet(report);

            MarkerOptions opts = new MarkerOptions()
                    .position(new LatLng(report.latitude, report.longitude))
                    .title(report.title != null ? report.title : "Sans titre")
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(hue));

            Marker marker = googleMap.addMarker(opts);
            if (marker != null) {
                marker.setTag(report.id);
            }
        }
    }

    /**
     * Retourne la teinte du marqueur selon le statut du rapport.
     *
     * @param status statut du rapport
     * @return teinte BitmapDescriptorFactory
     */
    private float getMarkerHue(@Nullable ReportStatus status) {
        if (status == null) return BitmapDescriptorFactory.HUE_RED;
        switch (status) {
            case EN_ATTENTE: return BitmapDescriptorFactory.HUE_RED;
            case EN_COURS:   return BitmapDescriptorFactory.HUE_ORANGE;
            case RESOLU:     return BitmapDescriptorFactory.HUE_GREEN;
            case IGNORE:     return BitmapDescriptorFactory.HUE_AZURE;
            default:         return BitmapDescriptorFactory.HUE_RED;
        }
    }

    /**
     * Construit le texte du snippet affiché dans l'info-window.
     * Inclut : catégorie, statut, description courte.
     *
     * @param report rapport à décrire
     * @return texte formaté pour l'info-window
     */
    private String buildSnippet(ReportEntity report) {
        StringBuilder sb = new StringBuilder();
        if (report.category != null) {
            sb.append("📁 ").append(report.category.name()).append("\n");
        }
        if (report.status != null) {
            sb.append("📌 ").append(report.status.name()).append("\n");
        }
        if (report.description != null && !report.description.isEmpty()) {
            String desc = report.description.length() > 60
                    ? report.description.substring(0, 60) + "..."
                    : report.description;
            sb.append(desc);
        }
        return sb.toString();
    }

    // ─── Nettoyage ────────────────────────────────────────────────────────────

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binder         = null;
        googleMap      = null;
        selectionMarker = null;
        selectionCircle = null;
        userLocationCircle = null;
        userLocationMarker = null;
    }
}