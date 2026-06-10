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
import com.dermacare.clinic.data.api.ApiClient; // Nếu cần dùng BASE_URL cho link local
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

        // 1. Tạo chữ cái đầu (Initials)
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

        // 2. Xử lý hiển thị Ảnh hoặc Chữ cái
        if (d.avatarUrl != null && !d.avatarUrl.trim().isEmpty()) {
            // CÓ ẢNH: Ẩn chữ, Hiện hình
            holder.tvAvatarInitials.setVisibility(View.GONE);
            holder.imgAvatar.setVisibility(View.VISIBLE);

            // Xử lý nếu link là link relative (ví dụ /uploads/...)
            String finalUrl = d.avatarUrl;
            if (finalUrl.startsWith("/")) {
                finalUrl = ApiClient.BASE_URL + finalUrl.substring(1);
            }

            Glide.with(holder.itemView.getContext())
                    .load(finalUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(holder.imgAvatar);
        } else {
            // KHÔNG CÓ ẢNH: Hiện chữ, Ẩn hình, Xóa bộ nhớ Glide cũ
            holder.tvAvatarInitials.setVisibility(View.VISIBLE);
            holder.imgAvatar.setVisibility(View.GONE);
            Glide.with(holder.itemView.getContext()).clear(holder.imgAvatar); // QUAN TRỌNG ĐỂ TRÁNH LỖI CUỘN
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