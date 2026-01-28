package com.example.healthassistant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RiskAdapter extends RecyclerView.Adapter<RiskAdapter.ViewHolder> {
    public interface OnRiskClickListener {
        void onClick(Risk risk);
    }

    private final List<Risk> items = new ArrayList<>();
    private final OnRiskClickListener listener;

    public RiskAdapter(OnRiskClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Risk> risks) {
        items.clear();
        if (risks != null) {
            items.addAll(risks);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_risk, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Risk risk = items.get(position);
        holder.severity.setText(risk.getSeverity());
        holder.title.setText(risk.getTitle());
        holder.summary.setText(risk.getSummary());
        holder.itemView.setOnClickListener(v -> listener.onClick(risk));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView severity;
        TextView title;
        TextView summary;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            severity = itemView.findViewById(R.id.text_risk_severity);
            title = itemView.findViewById(R.id.text_risk_title);
            summary = itemView.findViewById(R.id.text_risk_summary);
        }
    }
}
