package com.example.healthassistant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.ViewHolder> {

    private List<Medicine> medicineList = new ArrayList<>();
    private OnItemLongClickListener longClickListener;
    private OnItemClickListener clickListener;
    private OnReminderClickListener reminderClickListener;
    private OnSelectionModeListener selectionModeListener;

    private boolean isSelectionMode = false;
    private Set<Medicine> selectedItems = new HashSet<>();

    public interface OnItemLongClickListener {
        void onItemLongClick(Medicine medicine);
    }

    public interface OnItemClickListener {
        void onItemClick(Medicine medicine);
    }

    public interface OnReminderClickListener {
        void onReminderClick(Medicine medicine);
    }

    public interface OnSelectionModeListener {
        void onSelectionModeChanged(boolean enabled);
        void onSelectionCountChanged(int count);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnReminderClickListener(OnReminderClickListener listener) {
        this.reminderClickListener = listener;
    }

    public void setOnSelectionModeListener(OnSelectionModeListener listener) {
        this.selectionModeListener = listener;
    }

    public void setMedicines(List<Medicine> medicines) {
        this.medicineList = medicines;
        // 刷新列表时，如果正在选择模式，清理无效的选择
        if (isSelectionMode) {
            Set<Medicine> newSelected = new HashSet<>();
            for (Medicine m : medicines) {
                // 这里简单通过名称判断是否还是同一个（实际建议通过ID判断）
                for (Medicine selected : selectedItems) {
                    if (selected.getId() == m.getId()) {
                        newSelected.add(m);
                        break;
                    }
                }
            }
            selectedItems = newSelected;
        }
        notifyDataSetChanged();
    }

    public boolean isSelectionMode() {
        return isSelectionMode;
    }

    public void setSelectionMode(boolean enabled) {
        if (this.isSelectionMode != enabled) {
            this.isSelectionMode = enabled;
            if (!enabled) {
                selectedItems.clear();
            }
            notifyDataSetChanged();
            if (selectionModeListener != null) {
                selectionModeListener.onSelectionModeChanged(enabled);
            }
        }
    }

    public void selectAll(boolean all) {
        if (all) {
            selectedItems.addAll(medicineList);
        } else {
            selectedItems.clear();
        }
        notifyDataSetChanged();
        if (selectionModeListener != null) {
            selectionModeListener.onSelectionCountChanged(selectedItems.size());
        }
    }

    public List<Medicine> getSelectedItems() {
        return new ArrayList<>(selectedItems);
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
            holder.imageReminder.setAlpha(1.0f);
        } else {
            holder.reminderTimeTv.setVisibility(View.GONE);
            holder.imageReminder.setAlpha(0.3f);
        }

        // 处理选择模式 UI
        if (isSelectionMode) {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(selectedItems.contains(medicine));
            holder.imageReminder.setVisibility(View.GONE); // 选择模式下隐藏闹钟图标防止干扰
        } else {
            holder.checkBox.setVisibility(View.GONE);
            holder.imageReminder.setVisibility(View.VISIBLE);
        }

        holder.checkBox.setOnClickListener(v -> {
            toggleSelection(medicine);
        });

        holder.imageReminder.setOnClickListener(v -> {
            if (reminderClickListener != null) {
                reminderClickListener.onReminderClick(medicine);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (isSelectionMode) {
                toggleSelection(medicine);
            } else if (clickListener != null) {
                clickListener.onItemClick(medicine);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (!isSelectionMode) {
                setSelectionMode(true);
                toggleSelection(medicine);
                return true;
            }
            if (longClickListener != null) {
                longClickListener.onItemLongClick(medicine);
            }
            return true;
        });
    }

    private void toggleSelection(Medicine medicine) {
        if (selectedItems.contains(medicine)) {
            selectedItems.remove(medicine);
        } else {
            selectedItems.add(medicine);
        }
        notifyDataSetChanged();
        if (selectionModeListener != null) {
            selectionModeListener.onSelectionCountChanged(selectedItems.size());
        }
    }

    @Override
    public int getItemCount() {
        return medicineList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTv, dosageTv, freqTv, reminderTimeTv;
        ImageView imageReminder;
        CheckBox checkBox;

        ViewHolder(View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.text_name);
            dosageTv = itemView.findViewById(R.id.text_dosage);
            freqTv = itemView.findViewById(R.id.text_frequency);
            reminderTimeTv = itemView.findViewById(R.id.text_reminder_time);
            imageReminder = itemView.findViewById(R.id.image_reminder);
            checkBox = itemView.findViewById(R.id.checkbox_select);
        }
    }
}