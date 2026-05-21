package com.dermacare.clinic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dermacare.clinic.R;
import com.dermacare.clinic.model.Doctor;

import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.Holder> {
    private final List<Doctor> doctors;

    public DoctorAdapter(List<Doctor> doctors) {
        this.doctors = doctors;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_doctor, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Doctor d = doctors.get(position);
        holder.tvDoctorName.setText(d.name);
        holder.tvSpecialty.setText(d.specialty);
        holder.tvRating.setText(d.rating);
        holder.tvAvailable.setVisibility(d.availableToday ? View.VISIBLE : View.GONE);
        Glide.with(holder.imgAvatar.getContext())
                .load(d.avatarUrl)
                .circleCrop()
                .into(holder.imgAvatar);
    }

    @Override
    public int getItemCount() {
        return doctors.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ImageView imgAvatar;
        final TextView tvDoctorName;
        final TextView tvSpecialty;
        final TextView tvRating;
        final TextView tvAvailable;

        Holder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
            tvSpecialty = itemView.findViewById(R.id.tvSpecialty);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvAvailable = itemView.findViewById(R.id.tvAvailable);
        }
    }
}
