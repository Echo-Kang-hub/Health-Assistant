package com.example.healthassistant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.ViewHolder> {
    private final List<Medication> items = new ArrayList<>();

    public void setItems(List<Medication> meds) {
        items.clear();
        if (meds != null) {
            items.addAll(meds);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medication, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Medication med = items.get(position);
        holder.name.setText(med.getName());
        String usage = med.getUsageText() != null ? med.getUsageText() : "用法待确认";
        holder.usage.setText(usage);
        holder.nextTime.setText("下一次时间待定");
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView usage;
        TextView nextTime;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_med_name);
            usage = itemView.findViewById(R.id.text_med_usage);
            nextTime = itemView.findViewById(R.id.text_med_next);
        }
    }
}
