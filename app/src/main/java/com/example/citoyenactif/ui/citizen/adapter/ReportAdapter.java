package com.example.citoyenactif.ui.citizen.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.citoyenactif.data.local.entity.ReportEntity;
import com.example.citoyenactif.databinding.ItemReportBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter RecyclerView pour afficher la liste des rapports du citoyen.
 *
 * Avec DataBinding, le titre, la description et la ligne "Catégorie • Statut"
 * sont affichés par le XML (item_report.xml). L'adapter donne seulement le
 * signalement au layout et branche le clic et l'appui long.
 */
public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportVH> {

    /** Callback appelé quand l'utilisateur clique sur un report. */
    public interface OnReportClickListener {
        void onClick(ReportEntity report);
    }

    /** Callback appelé quand l'utilisateur fait un appui long sur un report. */
    public interface OnReportLongClickListener {
        void onLongClick(ReportEntity report);
    }

    /** Liste interne des éléments affichés. */
    private final List<ReportEntity> items = new ArrayList<>();

    private final OnReportClickListener clickListener;
    private final OnReportLongClickListener longClickListener;

    /** Constructeur : on injecte les listeners (lambdas dans le fragment). */
    public ReportAdapter(OnReportClickListener clickListener,
                         OnReportLongClickListener longClickListener) {
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    /** Remplace la liste actuelle puis rafraîchit le RecyclerView. */
    public void setItems(List<ReportEntity> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    /** Retourne l'élément à une position donnée. */
    public ReportEntity getItem(int position) {
        return items.get(position);
    }

    @NonNull
    @Override
    public ReportVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemReportBinding binding = ItemReportBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ReportVH(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportVH holder, int position) {
        ReportEntity report = items.get(position);
        holder.bind(report, clickListener, longClickListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** ViewHolder : contient le binding de la ligne. */
    static class ReportVH extends RecyclerView.ViewHolder {

        private final ItemReportBinding binding;

        ReportVH(ItemReportBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ReportEntity report,
                  OnReportClickListener clickListener,
                  OnReportLongClickListener longClickListener) {

            // Le XML affiche titre, description et "Catégorie • Statut"
            binding.setReport(report);
            binding.setShowCitizen(false);   // le citoyen n'a pas besoin de voir son propre numéro

            // Clic simple : géré par le binding d'action du layout (on adapte l'interface)
            binding.setListener(r -> {
                if (clickListener != null) clickListener.onClick(r);
            });

            // Appui long : on délègue au fragment
            // return true => l'événement est consommé (pas de clic simple en plus)
            binding.getRoot().setOnLongClickListener(v -> {
                if (longClickListener != null) longClickListener.onLongClick(report);
                return true;
            });

            binding.executePendingBindings();
        }
    }
}