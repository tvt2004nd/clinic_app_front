package com.dermacare.clinic.doctor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dermacare.clinic.R;
import com.dermacare.clinic.data.api.model.AppointmentResponse;

import java.util.List;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.Holder> {

    private List<AppointmentResponse> items;

    public PatientAdapter(List<AppointmentResponse> items) {
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
                .inflate(R.layout.item_patient_card, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        AppointmentResponse appt = items.get(position);

        String name = appt.patientName != null ? appt.patientName : "Bệnh nhân";
        holder.tvName.setText(name);

        String phone = appt.patientPhone != null && !appt.patientPhone.isEmpty() ? appt.patientPhone : "Chưa cập nhật";
        holder.tvPhone.setText(phone);
        
        holder.tvRecentVisit.setText(appt.date != null ? appt.date : "--");

        // Generate initials
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

        holder.btnChat.setOnClickListener(v -> {
            // Need to fetch or create conversation first via API, but for MVP we can start ChatActivity
            // and let ChatActivity fetch the conversationId, or we can fetch it here.
            com.dermacare.clinic.data.api.ApiClient.getAppointmentService(v.getContext())
                    .getConversation(null, appt.patientId)
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
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView tvName, tvPhone, tvRecentVisit, tvInitials;
        final View btnChat;

        Holder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvPatientName);
            tvPhone = itemView.findViewById(R.id.tvPatientPhone);
            tvRecentVisit = itemView.findViewById(R.id.tvRecentVisit);
            tvInitials = itemView.findViewById(R.id.tvPatientInitials);
            btnChat = itemView.findViewById(R.id.btnChat);
        }
    }
}
