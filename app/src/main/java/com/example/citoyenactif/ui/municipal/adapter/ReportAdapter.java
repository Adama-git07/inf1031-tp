package com.example.citoyenactif.ui.municipal.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.citoyenactif.data.local.entity.ReportEntity;
import com.example.citoyenactif.databinding.ItemReportBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter de la liste des signalements (côté agent municipal).
 *
 * Avec DataBinding, l'adapter donne seulement le signalement et le listener
 * au layout item_report.xml : c'est le XML qui affiche les données et gère le clic.
 */
public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.VH> {

    /** Interface appelée quand on clique sur un signalement (utilisée dans le XML). */
    public interface OnReportClickListener {
        void onReportClick(ReportEntity report);
    }

    private final List<ReportEntity> items = new ArrayList<>();
    private final OnReportClickListener listener;

    public ReportAdapter(OnReportClickListener listener) {
        this.listener = listener;
    }

    /** Remplace la liste affichée. */
    public void submit(List<ReportEntity> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemReportBinding b = ItemReportBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        // On donne au layout le signalement et le listener : le XML fait le reste
        holder.b.setReport(items.get(position));
        holder.b.setListener(listener);
        holder.b.setShowCitizen(true);   // l'agent voit aussi le numéro du citoyen
        holder.b.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** ViewHolder qui garde le binding de la ligne. */
    static class VH extends RecyclerView.ViewHolder {
        final ItemReportBinding b;

        VH(ItemReportBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}