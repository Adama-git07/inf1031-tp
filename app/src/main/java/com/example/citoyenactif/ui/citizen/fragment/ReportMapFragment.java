package com.example.citoyenactif.ui.citizen.fragment;

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
import com.example.citoyenactif.ui.report.ReportActivity;
import com.example.citoyenactif.ui.viewmodel.ReportViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

/**
 * Fragment "Carte des rapports" — Google Maps SDK for Android.
 *
 * Architecture :
 *   - Le layout contient un FrameLayout (mapContainer)
 *   - onViewCreated y insère un SupportMapFragment via getChildFragmentManager()
 *   - onMapReady reçoit l'objet GoogleMap et ajoute les marqueurs
 *
 * Fonctionnalités :
 *   - Centré sur Trois-Rivières (46.35°N, 72.55°O), zoom 13
 *   - Chaque rapport géolocalisé → un Marker avec titre + extrait
 *   - Clic sur l'info-window d'un marqueur → ouvre ReportActivity en mode VIEW
 *   - FAB → crée un nouveau rapport (mode CREATE)
 *
 * Clé API :
 *   Déclarée dans res/values/strings.xml → google_maps_key
 *   Référencée dans AndroidManifest.xml via <meta-data com.google.android.geo.API_KEY>
 */
public class ReportMapFragment extends Fragment implements OnMapReadyCallback {

    private ReportsMapFragmentBinding binder;
    private GoogleMap googleMap;
    private ReportViewModel viewModel;

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

        // ── Insérer le SupportMapFragment dans le conteneur ───────────────────
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getChildFragmentManager()
                .beginTransaction()
                .replace(com.example.citoyenactif.R.id.mapContainer, mapFragment)
                .commit();

        // Callback déclenché quand la carte est prête
        mapFragment.getMapAsync(this);

        // ── ViewModel pour observer les rapports ──────────────────────────────
        viewModel = new ViewModelProvider(this).get(ReportViewModel.class);

        // ── FAB → créer un rapport ────────────────────────────────────────────
        binder.fabAddReport.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ReportActivity.class))
        );
    }

    // ─── OnMapReadyCallback ───────────────────────────────────────────────────

    /**
     * Appelé automatiquement quand la carte Google Maps est initialisée et prête.
     *
     * On configure ici :
     *   - Les contrôles UI (zoom, boussole)
     *   - La caméra initiale (Trois-Rivières)
     *   - L'observation des rapports Room → ajout/MAJ des marqueurs
     *   - Le clic sur l'info-window d'un marqueur → ouvrir le rapport
     */
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;

        // ── Configuration de la carte ──────────────────────────────────────────
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setMapToolbarEnabled(true);

        // Centre initial : Trois-Rivières, Québec
        LatLng troisRivieres = new LatLng(46.3498, -72.5529);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(troisRivieres, 13f));

        // ── Marqueurs des rapports ────────────────────────────────────────────
        viewModel.getAllReports().observe(getViewLifecycleOwner(), reports ->
                refreshMarkers(reports)
        );

        // ── Clic sur l'info-window → ouvrir le rapport en mode VIEW ───────────
        map.setOnInfoWindowClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag instanceof Integer) {
                Intent intent = new Intent(requireContext(), ReportActivity.class);
                intent.putExtra(ReportActivity.EXTRA_REPORT_ID, (int) tag);
                startActivity(intent);
            }
        });
    }

    /**
     * Efface tous les marqueurs existants et les recrée à partir de la liste Room.
     *
     * Chaque marqueur reçoit :
     *   - position  : latitude/longitude du rapport
     *   - title     : titre du rapport
     *   - snippet   : description (affichée dans l'info-window)
     *   - tag       : report.id (pour retrouver le rapport au clic)
     *
     * Les rapports sans coordonnées (lat=0, lng=0) sont ignorés.
     */
    private void refreshMarkers(@Nullable List<ReportEntity> reports) {
        if (googleMap == null) return;

        googleMap.clear();
        if (reports == null) return;

        for (ReportEntity report : reports) {

            // Ignorer les rapports non géolocalisés
            if (report.latitude == 0.0 && report.longitude == 0.0) continue;

            MarkerOptions opts = new MarkerOptions()
                    .position(new LatLng(report.latitude, report.longitude))
                    .title(report.title != null ? report.title : "Sans titre")
                    .snippet(report.description != null ? report.description : "");

            Marker marker = googleMap.addMarker(opts);
            if (marker != null) {
                // Tag = ID du rapport, utilisé lors du clic sur l'info-window
                marker.setTag(report.id);
            }
        }
    }

    // ─── Nettoyage ────────────────────────────────────────────────────────────

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binder = null;
        googleMap = null;
    }
}
