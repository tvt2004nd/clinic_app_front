package com.dermacare.clinic.doctor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dermacare.clinic.R;
import com.dermacare.clinic.data.api.model.AppointmentResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class PendingAppointmentAdapter extends RecyclerView.Adapter<PendingAppointmentAdapter.Holder> {

    public interface ActionListener {
        void onConfirm(AppointmentResponse appt);
        void onExamine(AppointmentResponse appt);
    }

    private List<AppointmentResponse> items;
    private final ActionListener listener;

    public PendingAppointmentAdapter(List<AppointmentResponse> items, ActionListener listener) {
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
                .inflate(R.layout.item_appointment, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        AppointmentResponse appt = items.get(position);

        String time = appt.time != null && appt.time.length() >= 5
                ? appt.time.substring(0, 5) : appt.time;
        holder.tvTime.setText(time != null ? time : "--");

        int hour = appt.time != null && appt.time.length() >= 2
                ? Integer.parseInt(appt.time.substring(0, 2)) : 8;
        holder.tvTimePeriod.setText(hour < 12 ? "SA" : "CH");

        holder.tvPatientName.setText(appt.patientName != null ? appt.patientName : "--");

        String reason = appt.reason != null && !appt.reason.isEmpty() ? appt.reason : "Khám da liễu";
        holder.tvType.setText(reason);

        if ("PENDING".equals(appt.status)) {
            holder.statusBar.setBackgroundResource(R.drawable.bg_status_bar_pending);
            holder.timeBlock.setCardBackgroundColor(0xFFFEF3C7);
            holder.tvTime.setTextColor(0xFFD97706);
            holder.tvTimePeriod.setTextColor(0xFFD97706);
            holder.tvStatusBadge.setText("Chờ xác nhận");
            holder.tvStatusBadge.setTextColor(0xFFD97706);
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_amber);

            holder.btnExamine.setText("Xác nhận");
            holder.btnExamine.setVisibility(View.VISIBLE);
            holder.btnExamine.setOnClickListener(v -> listener.onConfirm(appt));
        } else if ("CONFIRMED".equals(appt.status)) {
            holder.statusBar.setBackgroundResource(R.drawable.bg_status_bar_teal);
            holder.timeBlock.setCardBackgroundColor(0xFFE0F2F1);
            holder.tvTime.setTextColor(0xFF0D9488);
            holder.tvTimePeriod.setTextColor(0xFF0D9488);
            holder.tvStatusBadge.setText("Chờ khám");
            holder.tvStatusBadge.setTextColor(0xFF0D9488);
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_teal);

            holder.btnExamine.setText("Khám");
            holder.btnExamine.setVisibility(View.VISIBLE);
            holder.btnExamine.setOnClickListener(v -> listener.onExamine(appt));
        } else if ("COMPLETED".equals(appt.status)) {
            holder.statusBar.setBackgroundColor(0xFF4CAF50);
            holder.timeBlock.setCardBackgroundColor(0xFFE8F5E9);
            holder.tvTime.setTextColor(0xFF4CAF50);
            holder.tvTimePeriod.setTextColor(0xFF4CAF50);
            holder.tvStatusBadge.setText("Đã khám");
            holder.tvStatusBadge.setTextColor(0xFF4CAF50);

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

    static class Holder extends RecyclerView.ViewHolder {
        final View statusBar;
        final MaterialCardView timeBlock;
        final TextView tvTime, tvTimePeriod, tvPatientName, tvType, tvStatusBadge;
        final MaterialButton btnExamine;

        Holder(@NonNull View itemView) {
            super(itemView);
            statusBar = itemView.findViewById(R.id.statusBar);
            timeBlock = itemView.findViewById(R.id.timeBlock);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvTimePeriod = itemView.findViewById(R.id.tvTimePeriod);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvType = itemView.findViewById(R.id.tvType);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            btnExamine = itemView.findViewById(R.id.btnExamine);
        }
    }
}
