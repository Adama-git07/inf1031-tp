package com.example.citoyenactif.ui.citizen.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.citoyenactif.data.local.entity.ReportEntity;
import com.example.citoyenactif.databinding.ReportsMapFragmentBinding;
import com.example.citoyenactif.ui.BaseActivity;
import com.example.citoyenactif.ui.report.ReportActivity;
import com.example.citoyenactif.ui.viewmodel.ReportViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

/**
 * Fragment "Carte des rapports" — Google Maps SDK for Android.
 *
 * Fonctionnalités :
 *   - Centré sur Trois-Rivières (46.35°N, 72.55°O), zoom 13
 *   - Marqueurs bleus : rapports existants (tag = report.id : Integer)
 *     → clic sur l'info-window ouvre le rapport en mode VIEW
 *   - Marqueur rouge (pin de sélection) : clic sur la carte vide
 *     → tag = "new_report" (String)
 *     → clic sur l'info-window ouvre ReportActivity en mode CREATE
 *       avec EXTRA_LATITUDE / EXTRA_LONGITUDE pré-remplis
 *   - FAB → ouvre ReportActivity en mode CREATE sans coordonnées
 *
 * Accès au LocationViewModel :
 *   - Via ((BaseActivity) requireActivity()).getLocationViewModel()
 *   - Permet de mémoriser la position sélectionnée et de l'observer depuis ReportActivity
 */
public class ReportMapFragment extends Fragment implements OnMapReadyCallback {

    /** Tag spécial sur le marqueur de sélection (distinct des tags entiers des rapports). */
    private static final String TAG_NEW_REPORT = "new_report";

    private ReportsMapFragmentBinding binder;
    private GoogleMap googleMap;
    private ReportViewModel viewModel;

    /** Marqueur de sélection (clic carte) — un seul à la fois. */
    @Nullable
    private Marker selectionMarker = null;

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
                .replace(com.example.citoyenactif.R.id.mapContainer, mapFragment)
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

        // ── Configuration de la carte ──────────────────────────────────────────
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setMapToolbarEnabled(true);

        // Afficher la position de l'utilisateur si la permission est accordée
        if (requireActivity() instanceof BaseActivity
                && ((BaseActivity) requireActivity()).hasLocationPermission()) {
            map.setMyLocationEnabled(true);
        }

        // Centre initial : Trois-Rivières, Québec
        LatLng troisRivieres = new LatLng(46.3498, -72.5529);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(troisRivieres, 13f));

        // ── Marqueurs des rapports existants ──────────────────────────────────
        viewModel.getAllReports().observe(getViewLifecycleOwner(), this::refreshMarkers);

        // ── Clic sur la carte vide → marqueur de sélection (nouveau rapport) ──
        map.setOnMapClickListener(this::onMapClick);

        // ── Clic sur une info-window ──────────────────────────────────────────
        map.setOnInfoWindowClickListener(this::onInfoWindowClick);
    }

    // ─── Clic carte vide ─────────────────────────────────────────────────────

    /**
     * Appelé quand l'utilisateur touche un endroit vide de la carte.
     *
     * Place (ou déplace) un marqueur rouge avec l'info-window automatiquement ouverte.
     * Le tag "new_report" distingue ce marqueur des marqueurs de rapports existants.
     * Stocke aussi la position dans le LocationViewModel de l'activité parente
     * pour une éventuelle observation réactive.
     *
     * @param latLng coordonnées du point touché
     */
    private void onMapClick(LatLng latLng) {
        // Supprimer le marqueur précédent s'il existe
        if (selectionMarker != null) {
            selectionMarker.remove();
            selectionMarker = null;
        }

        // Créer un marqueur rouge à la position cliquée
        MarkerOptions opts = new MarkerOptions()
                .position(latLng)
                .title("Nouveau rapport ici")
                .snippet("Appuyez pour créer un rapport à cet emplacement.")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        selectionMarker = googleMap.addMarker(opts);

        if (selectionMarker != null) {
            // Tag String spécial → identifié dans onInfoWindowClick
            selectionMarker.setTag(TAG_NEW_REPORT);
            // Ouvrir l'info-window automatiquement
            selectionMarker.showInfoWindow();
        }

        // Mémoriser la position dans le LocationViewModel (architecture réactive)
        if (requireActivity() instanceof BaseActivity) {
            ((BaseActivity) requireActivity())
                    .getLocationViewModel()
                    .setSelectedMapLocation(latLng);
        }
    }

    // ─── Clic info-window ─────────────────────────────────────────────────────

    /**
     * Traite le clic sur l'info-window d'un marqueur selon son tag :
     *
     *   - tag Integer → rapport existant → ouvre en mode VIEW
     *   - tag "new_report" → ouvre en mode CREATE avec coordonnées pré-remplies
     */
    private void onInfoWindowClick(Marker marker) {
        Object tag = marker.getTag();

        if (tag instanceof Integer) {
            // ── Rapport existant → mode VIEW ──────────────────────────────────
            Intent intent = new Intent(requireContext(), ReportActivity.class);
            intent.putExtra(ReportActivity.EXTRA_REPORT_ID, (int) tag);
            startActivity(intent);

        } else if (TAG_NEW_REPORT.equals(tag)) {
            // ── Nouveau rapport depuis la carte → mode CREATE + coordonnées ────
            LatLng pos = marker.getPosition();
            Intent intent = new Intent(requireContext(), ReportActivity.class);
            intent.putExtra(ReportActivity.EXTRA_LATITUDE,  pos.latitude);
            intent.putExtra(ReportActivity.EXTRA_LONGITUDE, pos.longitude);
            startActivity(intent);

            // Nettoyer le marqueur de sélection après navigation
            selectionMarker.remove();
            selectionMarker = null;
        }
    }

    // ─── Marqueurs des rapports ───────────────────────────────────────────────

    /**
     * Efface tous les marqueurs de rapports et les recrée à partir de la liste Room.
     * Préserve le marqueur de sélection (rouge) s'il existe.
     *
     * Chaque marqueur :
     *   - position  : latitude/longitude du rapport
     *   - title     : titre du rapport
     *   - snippet   : description (affichée dans l'info-window)
     *   - tag       : report.id (Integer) — utilisé dans onInfoWindowClick
     *   - couleur   : bleue (distinct du rouge de sélection)
     */
    private void refreshMarkers(@Nullable List<ReportEntity> reports) {
        if (googleMap == null) return;

        // Supprimer uniquement les marqueurs de rapports (pas le marqueur de sélection)
        googleMap.clear();
        selectionMarker = null; // clear() a tout supprimé

        if (reports == null) return;

        for (ReportEntity report : reports) {
            if (report.latitude == 0.0 && report.longitude == 0.0) continue;

            MarkerOptions opts = new MarkerOptions()
                    .position(new LatLng(report.latitude, report.longitude))
                    .title(report.title != null ? report.title : "Sans titre")
                    .snippet(report.description != null ? report.description : "")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

            Marker marker = googleMap.addMarker(opts);
            if (marker != null) {
                marker.setTag(report.id); // Integer → rapport existant
            }
        }
    }

    // ─── Nettoyage ────────────────────────────────────────────────────────────

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binder = null;
        googleMap = null;
        selectionMarker = null;
    }
}
