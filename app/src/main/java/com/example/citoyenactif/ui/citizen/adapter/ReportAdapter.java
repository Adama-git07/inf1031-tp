package com.example.citoyenactif.ui.citizen.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.citoyenactif.data.local.entity.ReportEntity;
import com.example.citoyenactif.databinding.ItemReportBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter RecyclerView pour afficher une liste de rapports.
 */
public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportVH> {

    public interface OnReportClickListener {
        void onClick(ReportEntity report);
    }

    public interface OnReportLongClickListener {
        void onLongClick(ReportEntity report);
    }

    private final List<ReportEntity> items = new ArrayList<>();
    private final OnReportClickListener clickListener;
    private final OnReportLongClickListener longClickListener;

    public ReportAdapter(OnReportClickListener clickListener,
                         OnReportLongClickListener longClickListener) {
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    public void setItems(List<ReportEntity> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

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

    static class ReportVH extends RecyclerView.ViewHolder {

        private final ItemReportBinding binding;

        ReportVH(ItemReportBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ReportEntity report,
                  OnReportClickListener clickListener,
                  OnReportLongClickListener longClickListener) {

            binding.tvReportTitle.setText(report.title);

            if (report.category != null) {
                binding.tvReportMeta.setText(report.category.name()); // enum
            } else {
                binding.tvReportMeta.setText("—");
            }

            if (report.status != null) {
                binding.tvReportMeta.setText(report.status.name()); // enum
            } else {
                binding.tvReportMeta.setText("—");
            }

            binding.getRoot().setOnClickListener(v -> {
                if (clickListener != null) clickListener.onClick(report);
            });

            binding.getRoot().setOnLongClickListener(v -> {
                if (longClickListener != null) longClickListener.onLongClick(report);
                return true;
            });
        }
    }
}