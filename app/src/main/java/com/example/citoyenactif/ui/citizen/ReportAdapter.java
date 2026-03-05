package com.example.citoyenactif.ui.citizen;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.citoyenactif.data.local.entity.ReportEntity;
import com.example.citoyenactif.databinding.ItemReportBinding;

import java.util.ArrayList;
import java.util.List;

/** Adapter RecyclerView pour afficher la liste des rapports */
public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    public interface OnReportClickListener {
        void onClick(ReportEntity report);
    }

    public interface OnReportLongClickListener {
        void onLongClick(ReportEntity report);
    }

    private List<ReportEntity> reports = new ArrayList<>();
    private final OnReportClickListener clickListener;
    private OnReportLongClickListener longClickListener;

    public ReportAdapter(OnReportClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setLongClickListener(OnReportLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    public void setReports(List<ReportEntity> reports) {
        this.reports = reports;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemReportBinding binding = ItemReportBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ReportViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        ReportEntity report = reports.get(position);
        holder.binding.tvReportTitle.setText(report.title);
        holder.binding.tvReportCategory.setText(report.category != null ? report.category.name() : "");
        holder.binding.tvReportStatus.setText(report.status != null ? report.status.name() : "");

        // Clic simple
        holder.itemView.setOnClickListener(v -> clickListener.onClick(report));

        // Clic long = suppression
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) longClickListener.onLongClick(report);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        final ItemReportBinding binding;
        ReportViewHolder(ItemReportBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}