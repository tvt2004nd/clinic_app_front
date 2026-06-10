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
import com.dermacare.clinic.data.api.ApiClient;
import com.dermacare.clinic.data.api.model.AppointmentResponse; // Đã đổi sang file của bạn

import java.util.List;

public class DashboardPatientAdapter extends RecyclerView.Adapter<DashboardPatientAdapter.Holder> {

    private List<AppointmentResponse> list;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onClick(AppointmentResponse item);
    }

    public DashboardPatientAdapter(List<AppointmentResponse> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    public void setData(List<AppointmentResponse> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Gọi giao diện 1 dòng bệnh nhân ở trang chủ (Đã tạo ở các bước trước)
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dashboard_patient, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        AppointmentResponse item = list.get(position);

        // Hiển thị Giờ khám
        if (item.time != null && item.time.length() >= 5) {
            holder.tvTime.setText(item.time.substring(0, 5));
        } else {
            holder.tvTime.setText("--:--");
        }

        // Tên và Lý do
        String name = item.patientName != null ? item.patientName : "Bệnh nhân";
        holder.tvPatientName.setText(name);
        holder.tvReason.setText(item.reason != null && !item.reason.isEmpty() ? item.reason : "Khám tổng quát");

        // ================= XỬ LÝ AVATAR BẰNG GLIDE =================
        String avatarUrl = item.avatarUrl;

        if (avatarUrl != null && !avatarUrl.trim().isEmpty() && !avatarUrl.equals("null")) {
            // Có ảnh thật -> Ẩn chữ cái, hiện ImageView
            holder.tvPatientInitials.setVisibility(View.GONE);
            holder.ivPatientAvatar.setVisibility(View.VISIBLE);

            String finalUrl = avatarUrl.startsWith("/") ? ApiClient.BASE_URL + avatarUrl.substring(1) : avatarUrl;

            Glide.with(holder.itemView.getContext())
                    .load(finalUrl)
                    .placeholder(R.drawable.ic_nav_profile)
                    .error(R.drawable.ic_nav_profile)
                    .centerCrop()
                    .into(holder.ivPatientAvatar);
        } else {
            // Không có ảnh -> Hiện chữ cái, ẩn ImageView
            holder.tvPatientInitials.setVisibility(View.VISIBLE);
            holder.ivPatientAvatar.setVisibility(View.GONE);
            Glide.with(holder.itemView.getContext()).clear(holder.ivPatientAvatar);

            String initials = "";
            String[] parts = name.trim().split("\\s+");
            if (parts.length >= 2) {
                initials = String.valueOf(parts[parts.length - 2].charAt(0)) + parts[parts.length - 1].charAt(0);
            } else if (name.length() >= 2) {
                initials = name.substring(0, 2);
            } else if (!name.isEmpty()) {
                initials = name;
            }
            holder.tvPatientInitials.setText(initials.toUpperCase());
        }

        // Sự kiện Click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView tvTime, tvPatientName, tvReason, tvPatientInitials;
        ImageView ivPatientAvatar;

        Holder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvReason = itemView.findViewById(R.id.tvReason);
            tvPatientInitials = itemView.findViewById(R.id.tvPatientInitials);
            ivPatientAvatar = itemView.findViewById(R.id.ivPatientAvatar);
        }
    }
}