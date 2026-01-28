package com.example.healthassistant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DataSourceAdapter extends RecyclerView.Adapter<DataSourceAdapter.ViewHolder> {
    public interface OnActionClickListener {
        void onActionClick(DataSource source, boolean connect);
    }

    private final List<DataSource> items = new ArrayList<>();
    private final OnActionClickListener listener;

    public DataSourceAdapter(OnActionClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<DataSource> sources) {
        items.clear();
        if (sources != null) {
            items.addAll(sources);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_data_source, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DataSource source = items.get(position);
        holder.type.setText(source.getType());
        boolean connected = "connected".equalsIgnoreCase(source.getStatus());
        holder.status.setText(connected ? "已连接" : "未连接");
        holder.action.setText(connected ? "断开" : "连接");
        holder.action.setOnClickListener(v -> listener.onActionClick(source, !connected));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView type;
        TextView status;
        Button action;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            type = itemView.findViewById(R.id.text_source_type);
            status = itemView.findViewById(R.id.text_source_status);
            action = itemView.findViewById(R.id.button_source_action);
        }
    }
}
