package com.example.healthassistant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.ViewHolder> {

    private List<Medicine> medicineList = new ArrayList<>();
    private OnItemLongClickListener longClickListener;
    private OnItemClickListener clickListener;
    private OnReminderClickListener reminderClickListener; // 新增：提醒点击监听

    public interface OnItemLongClickListener {
        void onItemLongClick(Medicine medicine);
    }

    public interface OnItemClickListener {
        void onItemClick(Medicine medicine);
    }

    public interface OnReminderClickListener { // 新增：提醒点击接口
        void onReminderClick(Medicine medicine);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnReminderClickListener(OnReminderClickListener listener) { // 新增
        this.reminderClickListener = listener;
    }

    public void setMedicines(List<Medicine> medicines) {
        this.medicineList = medicines;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicine, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Medicine medicine = medicineList.get(position);
        holder.nameTv.setText(medicine.getName());
        holder.dosageTv.setText("剂量: " + medicine.getDosage());
        holder.freqTv.setText("频率: " + medicine.getFrequency());

        // 处理提醒 UI
        if (medicine.isReminderEnabled() && medicine.getReminderTime() != null && !medicine.getReminderTime().isEmpty()) {
            holder.reminderTimeTv.setText("提醒: " + medicine.getReminderTime());
            holder.reminderTimeTv.setVisibility(View.VISIBLE);
            holder.imageReminder.setAlpha(1.0f); // 高亮图标
        } else {
            holder.reminderTimeTv.setVisibility(View.GONE);
            holder.imageReminder.setAlpha(0.3f); // 暗淡图标
        }

        // 提醒图标点击
        holder.imageReminder.setOnClickListener(v -> {
            if (reminderClickListener != null) {
                reminderClickListener.onReminderClick(medicine);
            }
        });

        // 整个卡片点击
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(medicine);
            }
        });

        // 长按监听
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(medicine);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return medicineList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTv, dosageTv, freqTv, reminderTimeTv;
        ImageView imageReminder;

        ViewHolder(View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.text_name);
            dosageTv = itemView.findViewById(R.id.text_dosage);
            freqTv = itemView.findViewById(R.id.text_frequency);
            reminderTimeTv = itemView.findViewById(R.id.text_reminder_time);
            imageReminder = itemView.findViewById(R.id.image_reminder);
        }
    }
}