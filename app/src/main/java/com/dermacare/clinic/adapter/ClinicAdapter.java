package com.dermacare.clinic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dermacare.clinic.R;
import com.dermacare.clinic.model.Clinic;

import java.util.List;

public class ClinicAdapter extends RecyclerView.Adapter<ClinicAdapter.ClinicViewHolder> {

    public interface OnClinicActionListener {
        void onEdit(Clinic clinic);
        void onDelete(Clinic clinic);
    }

    private final List<Clinic> clinics;
    private final OnClinicActionListener listener;

    public ClinicAdapter(List<Clinic> clinics, OnClinicActionListener listener) {
        this.clinics = clinics;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ClinicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_clinic, parent, false);
        return new ClinicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClinicViewHolder holder, int position) {
        Clinic clinic = clinics.get(position);
        holder.tvName.setText(clinic.getName());
        holder.tvAddress.setText(clinic.getAddress());
        holder.tvPhone.setText(clinic.getPhone());
        
        // Placeholder image handling - in real app use Glide
        holder.ivClinic.setImageResource(R.drawable.ic_specialty_skin);

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(clinic));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(clinic));
    }

    @Override
    public int getItemCount() {
        return clinics.size();
    }

    static class ClinicViewHolder extends RecyclerView.ViewHolder {
        ImageView ivClinic;
        TextView tvName, tvAddress, tvPhone;
        ImageButton btnEdit, btnDelete;

        public ClinicViewHolder(@NonNull View itemView) {
            super(itemView);
            ivClinic = itemView.findViewById(R.id.ivClinic);
            tvName = itemView.findViewById(R.id.tvClinicName);
            tvAddress = itemView.findViewById(R.id.tvClinicAddress);
            tvPhone = itemView.findViewById(R.id.tvClinicPhone);
            btnEdit = itemView.findViewById(R.id.btnEditClinic);
            btnDelete = itemView.findViewById(R.id.btnDeleteClinic);
        }
    }
}
