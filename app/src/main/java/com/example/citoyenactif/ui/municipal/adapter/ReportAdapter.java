package com.example.citoyenactif.ui.municipal.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.citoyenactif.data.local.entity.ReportEntity;
import com.example.citoyenactif.databinding.ItemReportBinding;

import java.util.ArrayList;
import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.VH> {

    public interface OnReportClickListener {
        void onReportClick(ReportEntity report);
    }

    private final List<ReportEntity> items = new ArrayList<>();
    private final OnReportClickListener listener;

    public ReportAdapter(OnReportClickListener listener) {
        this.listener = listener;
    }

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
        ReportEntity r = items.get(position);

        holder.b.tvReportTitle.setText(r.title != null ? r.title : "");
        holder.b.tvReportDesc.setText(r.description != null ? r.description : "");

        String category = (r.category != null) ? r.category.name() : "—";
        String status = (r.status != null) ? r.status.name() : "—";
        String meta = category + " • " + status + " • citoyen #" + r.citizenId;
        holder.b.tvReportMeta.setText(meta);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onReportClick(r);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemReportBinding b;
        VH(ItemReportBinding b) { super(b.getRoot()); this.b = b; }
    }
}