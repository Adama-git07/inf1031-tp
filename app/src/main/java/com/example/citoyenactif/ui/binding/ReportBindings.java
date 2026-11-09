package com.example.citoyenactif.ui.binding;

import android.widget.TextView;

import androidx.databinding.BindingAdapter;

import com.example.citoyenactif.data.local.entity.ReportEntity;

/**
 * Extensions de binding personnalisées pour les signalements.
 *
 * Un @BindingAdapter crée de nouveaux attributs XML (ici app:reportMeta et
 * app:showCitizen) qui s'appliquent seulement au type de vue indiqué (TextView).
 */
public final class ReportBindings {

    private ReportBindings() { }

    /**
     * app:reportMeta="@{report}" : affiche "CATÉGORIE • STATUT".
     * Si app:showCitizen est vrai, ajoute aussi " • citoyen #id".
     * Gère les valeurs nulles (affiche "—").
     */
    @BindingAdapter(value = {"reportMeta", "showCitizen"}, requireAll = false)
    public static void setReportMeta(TextView textView, ReportEntity report, boolean showCitizen) {
        if (textView == null) return;
        if (report == null) {
            textView.setText("");
            return;
        }
        String category = (report.category != null) ? report.category.name() : "—";
        String status = (report.status != null) ? report.status.name() : "—";
        String text = category + " • " + status;
        if (showCitizen) {
            text += " • citoyen #" + report.citizenId;
        }
        textView.setText(text);
    }
}