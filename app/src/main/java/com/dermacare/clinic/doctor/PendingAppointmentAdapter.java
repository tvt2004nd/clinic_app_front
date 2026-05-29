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

        // Time block
        String time = appt.time != null && appt.time.length() >= 5
                ? appt.time.substring(0, 5) : appt.time;
        holder.tvTime.setText(time);

        holder.tvPatientName.setText(appt.patientName != null ? appt.patientName : "--");

        // Reason as type
        String reason = appt.reason != null && !appt.reason.isEmpty() ? appt.reason : "Chưa có triệu chứng";
        holder.tvType.setText(reason);

        // Status determines button label
        if ("PENDING".equals(appt.status)) {
            holder.btnExamine.setText("Xác nhận");
            holder.btnExamine.setVisibility(View.VISIBLE);
            holder.btnExamine.setOnClickListener(v -> listener.onConfirm(appt));
        } else if ("CONFIRMED".equals(appt.status)) {
            holder.btnExamine.setText("Khám");
            holder.btnExamine.setVisibility(View.VISIBLE);
            holder.btnExamine.setOnClickListener(v -> listener.onExamine(appt));
        } else {
            holder.btnExamine.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView tvTime, tvPatientName, tvType;
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
