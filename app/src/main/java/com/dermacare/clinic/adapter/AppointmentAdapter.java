package com.dermacare.clinic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.dermacare.clinic.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.Holder> {
    public interface OnExamineClick {
        void onExamine(int position);
    }

    private final List<String[]> items;
    @Nullable
    private final OnExamineClick listener;

    public AppointmentAdapter(List<String[]> items, @Nullable OnExamineClick listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        String[] row = items.get(position);
        holder.tvTime.setText(row[0]);
        holder.tvPatientName.setText(row[1]);
        holder.tvType.setText(row[2]);
        if (listener != null) {
            holder.btnExamine.setVisibility(View.VISIBLE);
            holder.btnExamine.setOnClickListener(v -> listener.onExamine(position));
        } else {
            holder.btnExamine.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView tvTime;
        final TextView tvPatientName;
        final TextView tvType;
        final MaterialButton btnExamine;

        Holder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvType = itemView.findViewById(R.id.tvType);
            btnExamine = itemView.findViewById(R.id.btnExamine);
        }
    }
}
