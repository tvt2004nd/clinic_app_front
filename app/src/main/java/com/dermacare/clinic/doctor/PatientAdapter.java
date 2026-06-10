package com.dermacare.clinic.doctor; // Hoặc package adapter của bạn

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
import com.dermacare.clinic.data.api.model.DoctorPatientResponse;

import java.util.List;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.Holder> {

    private List<DoctorPatientResponse> items;
    private OnPatientClickListener listener; // THÊM BIẾN LẮNG NGHE SỰ KIỆN

    // TẠO INTERFACE ĐỂ BẮT SỰ KIỆN CLICK VÀO BỆNH NHÂN
    public interface OnPatientClickListener {
        void onPatientClick(DoctorPatientResponse patient);
    }

    // CẬP NHẬT LẠI HÀM KHỞI TẠO ĐỂ NHẬN SỰ KIỆN
    public PatientAdapter(List<DoctorPatientResponse> items, OnPatientClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setData(List<DoctorPatientResponse> data) {
        this.items = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_patient_card, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        DoctorPatientResponse patient = items.get(position);

        String name = patient.fullName != null ? patient.fullName : "Bệnh nhân";
        holder.tvName.setText(name);

        String phone = patient.phone != null && !patient.phone.isEmpty() ? patient.phone : "Chưa cập nhật";
        holder.tvPhone.setText(phone);

        holder.tvRecentVisit.setText(patient.lastVisitDate != null ? patient.lastVisitDate : "--");

        // ================= XỬ LÝ ẢNH BẰNG GLIDE =================
        if (patient.avatarUrl != null && !patient.avatarUrl.trim().isEmpty()) {
            holder.tvInitials.setVisibility(View.GONE);
            holder.ivAvatar.setVisibility(View.VISIBLE);

            String finalUrl = patient.avatarUrl;
            if (finalUrl.startsWith("/")) {
                finalUrl = ApiClient.BASE_URL + finalUrl.substring(1);
            }

            Glide.with(holder.itemView.getContext())
                    .load(finalUrl)
                    .placeholder(R.drawable.ic_nav_profile)
                    .error(R.drawable.ic_nav_profile)
                    .circleCrop()
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setVisibility(View.GONE);
            holder.tvInitials.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext()).clear(holder.ivAvatar);

            String initials = "";
            String[] parts = name.trim().split("\\s+");
            if (parts.length >= 2) {
                initials = String.valueOf(parts[parts.length - 2].charAt(0)) + parts[parts.length - 1].charAt(0);
            } else if (name.length() >= 2) {
                initials = name.substring(0, 2);
            } else if (!name.isEmpty()) {
                initials = name;
            }
            holder.tvInitials.setText(initials.toUpperCase());
        }

        // ================= SỰ KIỆN CLICK =================

        // 1. CLICK VÀO NÚT CHAT BÊN PHẢI -> MỞ MÀN HÌNH CHAT
        holder.btnChat.setOnClickListener(v -> {
            ApiClient.getAppointmentService(v.getContext())
                    .getConversation(null, patient.patientId)
                    .enqueue(new retrofit2.Callback<java.util.Map<String, Object>>() {
                        @Override
                        public void onResponse(retrofit2.Call<java.util.Map<String, Object>> call, retrofit2.Response<java.util.Map<String, Object>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Number convIdObj = (Number) response.body().get("conversationId");
                                Long convId = convIdObj.longValue();
                                android.content.Intent intent = new android.content.Intent(v.getContext(), com.dermacare.clinic.chat.ChatActivity.class);
                                intent.putExtra("conversationId", convId);
                                intent.putExtra("chatTitle", name);
                                v.getContext().startActivity(intent);
                            }
                        }
                        @Override
                        public void onFailure(retrofit2.Call<java.util.Map<String, Object>> call, Throwable t) {}
                    });
        });

        // 2. CLICK VÀO TOÀN BỘ THẺ BỆNH NHÂN -> XEM LỊCH SỬ KHÁM
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPatientClick(patient);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView tvName, tvPhone, tvRecentVisit, tvInitials;
        final ImageView ivAvatar;
        final View btnChat;

        Holder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvPatientName);
            tvPhone = itemView.findViewById(R.id.tvPatientPhone);
            tvRecentVisit = itemView.findViewById(R.id.tvRecentVisit);
            tvInitials = itemView.findViewById(R.id.tvPatientInitials);
            ivAvatar = itemView.findViewById(R.id.imgPatientAvatar);
            btnChat = itemView.findViewById(R.id.btnChat);
        }
    }
}