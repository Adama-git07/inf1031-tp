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
 * Adapter RecyclerView pour afficher une liste de rapports (ReportEntity).
 *
 * Rôle :
 * - Contenir la liste des reports à afficher
 * - Créer les ViewHolder (inflater item_report.xml)
 * - Binder (remplir) les vues pour chaque report
 * - Remonter les événements de clic (click / long click) au Fragment/Activity
 */
public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportVH> {

    /**
     * Callback appelé quand l'utilisateur clique sur un report.
     * Le fragment/activité peut décider quoi faire (ouvrir détail, etc.)
     */
    public interface OnReportClickListener {
        void onClick(ReportEntity report);
    }

    /**
     * Callback appelé quand l'utilisateur fait un appui long sur un report.
     * Souvent utilisé pour afficher un menu (supprimer / modifier) ou sélectionner l'item.
     */
    public interface OnReportLongClickListener {
        void onLongClick(ReportEntity report);
    }

    /**
     * Liste interne des éléments affichés.
     * On utilise une ArrayList pour pouvoir clear/add facilement.
     */
    private final List<ReportEntity> items = new ArrayList<>();

    /**
     * Listeners fournis depuis l'extérieur (Fragment/Activity) pour gérer les actions utilisateur.
     */
    private final OnReportClickListener clickListener;
    private final OnReportLongClickListener longClickListener;

    /**
     * Constructeur : on injecte les listeners.
     * (Ils peuvent être des lambdas dans le fragment.)
     */
    public ReportAdapter(OnReportClickListener clickListener,
                         OnReportLongClickListener longClickListener) {
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    /**
     * Remplace entièrement la liste actuelle par une nouvelle liste puis rafraîchit le RecyclerView.
     * notifyDataSetChanged() rafraîchit tout (simple, mais pas le plus performant).
     */
    public void setItems(List<ReportEntity> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    /**
     * Retourne l'élément à une position donnée.
     * Utile pour récupérer un report lors d'un swipe ou d'une action contextuelle.
     */
    public ReportEntity getItem(int position) {
        return items.get(position);
    }

    /**
     * Crée le ViewHolder :
     * - on inflate le layout XML de l'item (item_report.xml via DataBinding)
     * - on renvoie un ReportVH qui encapsule le binding
     */
    @NonNull
    @Override
    public ReportVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemReportBinding binding = ItemReportBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ReportVH(binding);
    }

    /**
     * Associe (bind) le report à afficher au ViewHolder de la ligne.
     */
    @Override
    public void onBindViewHolder(@NonNull ReportVH holder, int position) {
        ReportEntity report = items.get(position);
        holder.bind(report, clickListener, longClickListener);
    }

    /**
     * Indique au RecyclerView combien d'items on a.
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolder :
     * - Contient les vues de l'item (via ItemReportBinding)
     * - Sait comment afficher un ReportEntity dans ces vues
     */
    static class ReportVH extends RecyclerView.ViewHolder {

        /** Binding généré (accès direct aux vues : tvReportTitle, tvReportMeta, etc.) */
        private final ItemReportBinding binding;

        ReportVH(ItemReportBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * Remplit la vue avec les données du report + branche les listeners.
         *
         * @param report report à afficher
         * @param clickListener callback clic
         * @param longClickListener callback appui long
         */
        void bind(ReportEntity report,
                  OnReportClickListener clickListener,
                  OnReportLongClickListener longClickListener) {

            // --- 1) Affichage du titre du report ---
            binding.tvReportTitle.setText(report.title);

            // --- 2) Affichage "Catégorie • Statut" ---
            // On gère les valeurs null pour éviter un crash (NullPointerException).
            String category = (report.category != null) ? report.category.name() : "—";
            String status = (report.status != null) ? report.status.name() : "—";

            // \u2022 = caractère "•"
            binding.tvReportMeta.setText(category + " \u2022 " + status);

            // --- 3) Clic simple : on délègue au fragment/activité ---
            binding.getRoot().setOnClickListener(v -> {
                if (clickListener != null) clickListener.onClick(report);
            });

            // --- 4) Appui long : on délègue aussi ---
            // return true => l'événement est consommé (on ne déclenche pas aussi le clic simple)
            binding.getRoot().setOnLongClickListener(v -> {
                if (longClickListener != null) longClickListener.onLongClick(report);
                return true;
            });
        }
    }
}