package com.dermacare.clinic.doctor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // Import ImageView
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Import Glide
import com.dermacare.clinic.R;
import com.dermacare.clinic.data.api.ApiClient;
import com.dermacare.clinic.data.api.model.AppointmentResponse;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Locale;

public class DoctorDashboardAppointmentAdapter extends RecyclerView.Adapter<DoctorDashboardAppointmentAdapter.Holder> {

    public interface ActionListener {
        void onConfirm(AppointmentResponse appt);
        void onExamine(AppointmentResponse appt);
        void onViewRecord(AppointmentResponse appt);
    }

    private static final int[][] AVATAR_STYLES = {
            {0xFFFEE2E2, 0xFFDC2626},
            {0xFFE0F2FE, 0xFF2563EB},
            {0xFFF3E8FF, 0xFF7C3AED}
    };

    private List<AppointmentResponse> items;
    private final ActionListener listener;

    public DoctorDashboardAppointmentAdapter(List<AppointmentResponse> items, ActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setData(List<AppointmentResponse> data) {
        this.items = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_doctor_dashboard_appointment, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        AppointmentResponse appt = items.get(position);

        String time = appt.time != null && appt.time.length() >= 5
                ? appt.time.substring(0, 5) : (appt.time != null ? appt.time : "--");
        holder.tvTime.setText(time);

        holder.tvPatientName.setText(appt.patientName != null ? appt.patientName : "--");

        // ================= XỬ LÝ AVATAR (GLIDE + CHỮ MÀU RANDOM) =================
        String avatarUrl = appt.avatarUrl; // Đảm bảo Model AppointmentResponse của bạn đã có biến này

        if (avatarUrl != null && !avatarUrl.trim().isEmpty() && !avatarUrl.equals("null")) {
            // 1. CÓ ẢNH THẬT: Tắt chữ, bật ảnh
            holder.tvAvatarInitials.setVisibility(View.GONE);
            holder.ivPatientAvatar.setVisibility(View.VISIBLE);

            String finalUrl = avatarUrl.startsWith("/") ? ApiClient.BASE_URL + avatarUrl.substring(1) : avatarUrl;

            Glide.with(holder.itemView.getContext())
                    .load(finalUrl)
                    .centerCrop()
                    .into(holder.ivPatientAvatar);
        } else {
            // 2. KHÔNG CÓ ẢNH: Bật chữ, tắt ảnh, clear bộ nhớ
            holder.tvAvatarInitials.setVisibility(View.VISIBLE);
            holder.ivPatientAvatar.setVisibility(View.GONE);
            Glide.with(holder.itemView.getContext()).clear(holder.ivPatientAvatar);

            holder.tvAvatarInitials.setText(getInitials(appt.patientName));

            // Xử lý đổi màu theo thuật toán tuyệt hay của bạn
            int[] avatarStyle = AVATAR_STYLES[position % AVATAR_STYLES.length];
            holder.cardAvatar.setCardBackgroundColor(avatarStyle[0]);
            holder.tvAvatarInitials.setTextColor(avatarStyle[1]);
        }
        // =========================================================================

        String specialty = appt.specialty != null && !appt.specialty.isEmpty()
                ? appt.specialty : "Da liễu";
        String visitType = appt.reason != null && !appt.reason.isEmpty() ? appt.reason : "Khám";
        holder.tvType.setText(specialty + " - " + visitType);

        boolean urgent = isUrgent(appt);
        if (urgent) {
            applyStatus(holder, R.drawable.bg_status_bar_red, R.drawable.bg_badge_red,
                    0xFFDC2626, "Khẩn cấp");
        } else if ("PENDING".equals(appt.status)) {
            applyStatus(holder, R.drawable.bg_status_bar_pending, R.drawable.bg_badge_amber,
                    0xFFD97706, "Chưa xác nhận");
        } else if ("CONFIRMED".equals(appt.status)) {
            applyStatus(holder, R.drawable.bg_status_bar_green, R.drawable.bg_badge_green,
                    0xFF059669, "Chưa khám");
        } else if ("COMPLETED".equals(appt.status)) {
            applyStatus(holder, R.drawable.bg_status_bar_teal, R.drawable.bg_badge_teal,
                    0xFF0D9488, "Đã khám");
        } else {
            applyStatus(holder, R.drawable.bg_status_bar_teal, R.drawable.bg_badge_teal,
                    0xFF0D9488, appt.status != null ? appt.status : "--");
        }

        holder.itemView.setOnClickListener(v -> {
            if ("PENDING".equals(appt.status)) {
                listener.onConfirm(appt);
            } else if ("CONFIRMED".equals(appt.status)) {
                listener.onExamine(appt);
            } else if ("COMPLETED".equals(appt.status)) {
                listener.onViewRecord(appt);
            }
        });
    }

    private static boolean isUrgent(AppointmentResponse appt) {
        String text = ((appt.reason != null ? appt.reason : "") + " "
                + (appt.specialty != null ? appt.specialty : "")).toLowerCase(Locale.ROOT);
        return text.contains("khẩn") || text.contains("gấp") || text.contains("ưu tiên");
    }

    private static void applyStatus(Holder holder, int barRes, int badgeRes, int textColor, String label) {
        holder.statusBar.setBackgroundResource(barRes);
        holder.tvStatusBadge.setBackgroundResource(badgeRes);
        holder.tvStatusBadge.setText(label);
        holder.tvStatusBadge.setTextColor(textColor);
    }

    private static String getInitials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase(Locale.ROOT);
        }
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase(Locale.ROOT);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class Holder extends RecyclerView.ViewHolder {
        final View statusBar;
        final MaterialCardView cardAvatar;
        final TextView tvAvatarInitials, tvPatientName, tvType, tvTime, tvStatusBadge;
        final ImageView ivPatientAvatar; // KAI BÁO IMAGEVIEW MỚI THÊM TỪ XML

        Holder(@NonNull View itemView) {
            super(itemView);
            statusBar = itemView.findViewById(R.id.statusBar);
            cardAvatar = itemView.findViewById(R.id.cardAvatar);
            tvAvatarInitials = itemView.findViewById(R.id.tvAvatarInitials);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvType = itemView.findViewById(R.id.tvType);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            ivPatientAvatar = itemView.findViewById(R.id.ivPatientAvatar); // ÁNH XẠ
        }
    }
}