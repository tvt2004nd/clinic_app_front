package com.dermacare.clinic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dermacare.clinic.R;
import com.dermacare.clinic.model.Doctor;

import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.Holder> {
    private List<Doctor> doctors;
    private OnDoctorClickListener listener;

    public interface OnDoctorClickListener {
        void onDoctorClick(Doctor doctor);
    }

    public DoctorAdapter(List<Doctor> doctors, OnDoctorClickListener listener) {
        this.doctors = doctors;
        this.listener = listener;
    }

    public void setDoctors(List<Doctor> doctors) {
        this.doctors = doctors;
        notifyDataSetChanged();
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
        holder.badgeAvailable.setVisibility(d.availableToday ? View.VISIBLE : View.GONE);
        holder.tvFee.setText(String.format("%,.0f₫", d.fee));

        // Build initials from name
        String initials = "";
        if (d.name != null && !d.name.isEmpty()) {
            String[] parts = d.name.trim().split("\\s+");
            if (parts.length >= 2) {
                initials = String.valueOf(parts[parts.length - 2].charAt(0))
                        + parts[parts.length - 1].charAt(0);
            } else {
                initials = parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
            }
        }
        holder.tvAvatarInitials.setText(initials.toUpperCase());
        holder.tvAvatarInitials.setVisibility(View.VISIBLE);

        if (d.avatarUrl != null && !d.avatarUrl.isEmpty()) {
            Glide.with(holder.imgAvatar.getContext())
                    .load(d.avatarUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .circleCrop()
                    .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                        @Override
                        public boolean onLoadFailed(com.bumptech.glide.load.engine.GlideException e,
                                Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                boolean isFirstResource) {
                            holder.tvAvatarInitials.setVisibility(View.VISIBLE);
                            return false;
                        }
                        @Override
                        public boolean onResourceReady(android.graphics.drawable.Drawable resource,
                                Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                            holder.tvAvatarInitials.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(holder.imgAvatar);
        } else {
            holder.tvAvatarInitials.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION && listener != null) {
                listener.onDoctorClick(doctors.get(currentPos));
            }
        });
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
        final TextView tvAvatarInitials;
        final TextView tvFee;
        final View badgeAvailable;

        Holder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
            tvSpecialty = itemView.findViewById(R.id.tvSpecialty);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvAvailable = itemView.findViewById(R.id.tvAvailable);
            tvAvatarInitials = itemView.findViewById(R.id.tvAvatarInitials);
            tvFee = itemView.findViewById(R.id.tvFee);
            badgeAvailable = itemView.findViewById(R.id.badgeAvailable);
        }
    }
}

