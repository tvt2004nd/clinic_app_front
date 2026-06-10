package com.dermacare.clinic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dermacare.clinic.R;
import com.dermacare.clinic.data.api.ApiClient;
import com.dermacare.clinic.data.api.model.AppointmentResponse;
import androidx.appcompat.app.AlertDialog;

import java.util.List;

public class PatientAppointmentAdapter extends RecyclerView.Adapter<PatientAppointmentAdapter.Holder> {

    private List<AppointmentResponse> items;

    public PatientAppointmentAdapter(List<AppointmentResponse> items) {
        this.items = items;
    }

    public void setData(List<AppointmentResponse> data) {
        this.items = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        // Lấy dữ liệu để hiển thị ngay lập tức
        AppointmentResponse appt = items.get(position);

        String time = appt.time != null && appt.time.length() >= 5
                ? appt.time.substring(0, 5)
                : appt.time;
        holder.tvTime.setText(time != null ? time : "--");

        int hour = appt.time != null && appt.time.length() >= 2
                ? Integer.parseInt(appt.time.substring(0, 2))
                : 8;
        holder.tvTimePeriod.setText(hour < 12 ? "SA" : "CH");

        holder.tvPatientName.setText(appt.doctorName != null ? appt.doctorName : "--");

        String reason = appt.specialty != null ? appt.specialty : "Khám da liễu";
        if (appt.roomId != null) {
            reason = reason + " • Phòng " + appt.roomId;
        }
        holder.tvType.setText(reason);

        // Xử lý UI theo trạng thái
        updateUIByStatus(holder, appt);

        // Hiển thị/ẩn các nút dựa trên trạng thái
        if ("PENDING".equals(appt.status) || "CONFIRMED".equals(appt.status)) {
            holder.btnCancel.setVisibility(View.VISIBLE);
            holder.btnReschedule.setVisibility(View.VISIBLE);
        } else {
            holder.btnCancel.setVisibility(View.GONE);
            holder.btnReschedule.setVisibility(View.GONE);
        }

        // Xử lý sự kiện Đổi lịch - Sử dụng getBindingAdapterPosition() để tránh lỗi position
        holder.btnReschedule.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;
            AppointmentResponse currentAppt = items.get(currentPos);

            if (currentAppt.doctorId == null) {
                android.widget.Toast.makeText(v.getContext(), "Không tìm thấy thông tin bác sĩ để đổi lịch", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            android.content.Intent intent = new android.content.Intent(v.getContext(), com.dermacare.clinic.patient.BookingActivity.class);
            intent.putExtra(com.dermacare.clinic.patient.BookingActivity.EXTRA_DOCTOR_ID, currentAppt.doctorId.longValue());
            intent.putExtra(com.dermacare.clinic.patient.BookingActivity.EXTRA_DOCTOR_NAME, currentAppt.doctorName);
            intent.putExtra(com.dermacare.clinic.patient.BookingActivity.EXTRA_SPECIALTY, currentAppt.specialty);
            intent.putExtra(com.dermacare.clinic.patient.BookingActivity.EXTRA_APPOINTMENT_ID, currentAppt.appointmentId);
            
            // Tính initials từ tên bác sĩ
            String initials = "??";
            if (currentAppt.doctorName != null && !currentAppt.doctorName.isEmpty()) {
                String[] parts = currentAppt.doctorName.trim().split("\\s+");
                if (parts.length >= 2) {
                    initials = (parts[parts.length - 2].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
                } else if (parts.length == 1) {
                    initials = parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
                }
            }
            intent.putExtra(com.dermacare.clinic.patient.BookingActivity.EXTRA_INITIALS, initials);

            v.getContext().startActivity(intent);
        });

        // Xử lý sự kiện Hủy lịch
        holder.btnCancel.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;
            AppointmentResponse currentAppt = items.get(currentPos);

            new AlertDialog.Builder(v.getContext())
                    .setTitle("Hủy lịch")
                    .setMessage("Bạn có chắc muốn hủy lịch này?")
                    .setPositiveButton("Hủy", (d, w) -> {
                        java.util.Map<String, String> body = new java.util.HashMap<>();
                        body.put("reason", "Hủy bởi bệnh nhân");
                        ApiClient.getAppointmentService(v.getContext())
                                .cancelAppointment(currentAppt.appointmentId, body)
                                .enqueue(new retrofit2.Callback<java.util.Map<String, Object>>() {
                                    @Override
                                    public void onResponse(@NonNull retrofit2.Call<java.util.Map<String, Object>> call,
                                                         @NonNull retrofit2.Response<java.util.Map<String, Object>> response) {
                                        if (response.isSuccessful()) {
                                            int updatedPos = holder.getBindingAdapterPosition();
                                            if (updatedPos != RecyclerView.NO_POSITION) {
                                                items.get(updatedPos).status = "CANCELLED";
                                                notifyItemChanged(updatedPos);
                                                android.widget.Toast.makeText(v.getContext(), "Đã hủy lịch",
                                                        android.widget.Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            android.widget.Toast.makeText(v.getContext(), "Hủy thất bại: " + response.message(),
                                                            android.widget.Toast.LENGTH_LONG).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(@NonNull retrofit2.Call<java.util.Map<String, Object>> call,
                                                         @NonNull Throwable t) {
                                        android.widget.Toast.makeText(v.getContext(), "Lỗi kết nối: " + t.getMessage(),
                                                android.widget.Toast.LENGTH_SHORT).show();
                                    }
                                });
                    })
                    .setNegativeButton("Đóng", null)
                    .show();
        });
    }

    private void updateUIByStatus(Holder holder, AppointmentResponse appt) {
        if ("PENDING".equals(appt.status)) {
            holder.statusBar.setBackgroundResource(R.drawable.bg_status_bar_pending);
            holder.timeBlock.setCardBackgroundColor(0xFFFEF3C7);
            holder.tvTime.setTextColor(0xFFD97706);
            holder.tvTimePeriod.setTextColor(0xFFD97706);
            holder.tvStatusBadge.setText("Chờ xác nhận");
            holder.tvStatusBadge.setTextColor(0xFFD97706);
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_amber);
            holder.btnExamine.setVisibility(View.GONE);
        } else if ("CONFIRMED".equals(appt.status)) {
            holder.statusBar.setBackgroundResource(R.drawable.bg_status_bar_teal);
            holder.timeBlock.setCardBackgroundColor(0xFFE0F2F1);
            holder.tvTime.setTextColor(0xFF0D9488);
            holder.tvTimePeriod.setTextColor(0xFF0D9488);
            holder.tvStatusBadge.setText("Đã xác nhận");
            holder.tvStatusBadge.setTextColor(0xFF0D9488);
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_teal);
            holder.btnExamine.setVisibility(View.GONE);
        } else if ("COMPLETED".equals(appt.status)) {
            holder.statusBar.setBackgroundColor(0xFF4CAF50);
            holder.timeBlock.setCardBackgroundColor(0xFFE8F5E9);
            holder.tvTime.setTextColor(0xFF4CAF50);
            holder.tvTimePeriod.setTextColor(0xFF4CAF50);
            holder.tvStatusBadge.setText("Đã khám");
            holder.tvStatusBadge.setTextColor(0xFF4CAF50);
            holder.btnExamine.setVisibility(View.GONE);
        } else if ("CANCELLED".equals(appt.status)) {
            holder.statusBar.setBackgroundColor(0xFF9E9E9E);
            holder.timeBlock.setCardBackgroundColor(0xFFF5F5F5);
            holder.tvTime.setTextColor(0xFF9E9E9E);
            holder.tvTimePeriod.setTextColor(0xFF9E9E9E);
            holder.tvStatusBadge.setText("Đã hủy");
            holder.tvStatusBadge.setTextColor(0xFF9E9E9E);
            holder.btnExamine.setVisibility(View.GONE);
        } else {
            holder.statusBar.setBackgroundColor(0xFF9E9E9E);
            holder.timeBlock.setCardBackgroundColor(0xFFF5F5F5);
            holder.tvTime.setTextColor(0xFF9E9E9E);
            holder.tvTimePeriod.setTextColor(0xFF9E9E9E);
            holder.tvStatusBadge.setText(appt.status);
            holder.tvStatusBadge.setTextColor(0xFF9E9E9E);
            holder.btnExamine.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public static class Holder extends RecyclerView.ViewHolder {
        final View statusBar;
        final com.google.android.material.card.MaterialCardView timeBlock;
        final TextView tvTime, tvTimePeriod, tvPatientName, tvType, tvStatusBadge;
        final com.google.android.material.button.MaterialButton btnExamine;
        final com.google.android.material.button.MaterialButton btnCancel;
        final com.google.android.material.button.MaterialButton btnReschedule;

        public Holder(@NonNull View itemView) {
            super(itemView);
            statusBar = itemView.findViewById(R.id.statusBar);
            timeBlock = itemView.findViewById(R.id.timeBlock);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvTimePeriod = itemView.findViewById(R.id.tvTimePeriod);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvType = itemView.findViewById(R.id.tvType);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            btnExamine = itemView.findViewById(R.id.btnExamine);
            btnCancel = itemView.findViewById(R.id.btnCancel);
            btnReschedule = itemView.findViewById(R.id.btnReschedule);
        }
    }
}
