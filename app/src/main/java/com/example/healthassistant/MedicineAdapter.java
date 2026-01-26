package com.example.healthassistant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.ViewHolder> {

    private List<Medicine> medicineList = new ArrayList<>();
    private OnItemLongClickListener longClickListener;
    private OnItemClickListener clickListener; // 新增：点击监听

    public interface OnItemLongClickListener {
        void onItemLongClick(Medicine medicine);
    }

    public interface OnItemClickListener { // 新增：点击接口
        void onItemClick(Medicine medicine);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) { // 新增：设置点击监听
        this.clickListener = listener;
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

        // 设置点击监听
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(medicine);
            }
        });

        // 设置长按监听
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
        TextView nameTv, dosageTv, freqTv;

        ViewHolder(View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.text_name);
            dosageTv = itemView.findViewById(R.id.text_dosage);
            freqTv = itemView.findViewById(R.id.text_frequency);
        }
    }
}