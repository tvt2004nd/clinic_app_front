package com.dermacare.clinic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dermacare.clinic.R;
import com.dermacare.clinic.data.api.model.PatientRecordSummaryResponse;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PatientRecordsAdapter extends RecyclerView.Adapter<PatientRecordsAdapter.Holder> {

    public interface OnRecordClickListener {
        void onRecordClick(Long recordId);
    }

    private final List<PatientRecordSummaryResponse> records;
    private final OnRecordClickListener listener;

    public PatientRecordsAdapter(List<PatientRecordSummaryResponse> records, OnRecordClickListener listener) {
        this.records = records;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_patient_record, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        PatientRecordSummaryResponse r = records.get(position);

        String disease = r.diseaseName != null ? r.diseaseName : "Khám bệnh";
        holder.tvDiagnosis.setText(disease);

        String dateStr = r.examinedAt;
        if (dateStr != null && dateStr.length() >= 10) {
            holder.tvDate.setText(dateStr.substring(0, 10));
        } else {
            holder.tvDate.setText("--");
        }

        if (r.prescriptionCount != null && r.prescriptionCount > 0) {
            String medText = r.prescriptionCount + " loại thuốc";
            if (r.followUpDate != null && !r.followUpDate.isEmpty()) {
                medText += " · Tái khám " + r.followUpDate;
            }
            holder.tvMedication.setText(medText);
            holder.medicationStrip.setVisibility(View.VISIBLE);
        } else {
            if (r.followUpDate != null && !r.followUpDate.isEmpty()) {
                holder.tvMedication.setText("Tái khám " + r.followUpDate);
                holder.medicationStrip.setVisibility(View.VISIBLE);
            } else {
                holder.medicationStrip.setVisibility(View.GONE);
            }
        }

        holder.card.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRecordClick(r.recordId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final MaterialCardView card;
        final TextView tvDiagnosis;
        final TextView tvDate;
        final TextView tvMedication;
        final View medicationStrip;

        Holder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardRecord);
            tvDiagnosis = itemView.findViewById(R.id.tvDiagnosis);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvMedication = itemView.findViewById(R.id.tvMedication);
            medicationStrip = itemView.findViewById(R.id.medicationStrip);
        }
    }
}
