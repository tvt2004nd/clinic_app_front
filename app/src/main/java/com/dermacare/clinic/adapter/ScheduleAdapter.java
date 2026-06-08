package com.dermacare.clinic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dermacare.clinic.R;
import com.dermacare.clinic.data.api.model.ScheduleResponse;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.Holder> {

    private final List<ScheduleResponse> schedules;
    private int selectedPosition = -1;
    private OnScheduleSelectListener listener;

    public interface OnScheduleSelectListener {
        void onScheduleSelected(ScheduleResponse schedule);
    }

    public ScheduleAdapter(List<ScheduleResponse> schedules, OnScheduleSelectListener listener) {
        this.schedules = schedules;
        this.listener = listener;
    }

    public ScheduleResponse getSelected() {
        if (selectedPosition >= 0 && selectedPosition < schedules.size()) {
            return schedules.get(selectedPosition);
        }
        return null;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        ScheduleResponse s = schedules.get(position);
        boolean isSelected = position == selectedPosition;
        boolean isBooked = s.isFull || "FULL".equals(s.status) || "CANCELLED".equals(s.status);

        // Parse date "yyyy-MM-dd"
        String[] parts = s.date != null ? s.date.split("-") : new String[]{"", "", ""};
        holder.tvDay.setText(parts.length == 3 ? parts[2] : "--");
        holder.tvMonth.setText(parts.length == 3 ? "Th." + parts[1] : "--");

        // Shift label
        String startHour = s.startTime != null ? s.startTime.substring(0, 5) : "--";
        String endHour = s.endTime != null ? s.endTime.substring(0, 5) : "--";
        holder.tvTime.setText(startHour + " – " + endHour);

        int startH = s.startTime != null ? Integer.parseInt(s.startTime.substring(0, 2)) : 8;
        holder.tvLabel.setText(startH < 12 ? "Buổi sáng" : "Buổi chiều");

        // Booked badge
        if (isBooked) {
            holder.tvFullBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvFullBadge.setVisibility(View.GONE);
        }

        // Selected state
        if (isSelected) {
            holder.card.setCardBackgroundColor(0xFF6366F1);
            holder.card.setStrokeWidth(0);
            holder.tvDay.setTextColor(0xFFFFFFFF);
            holder.tvMonth.setTextColor(0xCCFFFFFF);
            holder.dateBlock.setCardBackgroundColor(0x33FFFFFF);
            holder.tvTime.setTextColor(0xFFFFFFFF);
            holder.tvLabel.setTextColor(0xCCFFFFFF);
            holder.selectIndicator.setCardBackgroundColor(0x33FFFFFF);
            holder.ivCheck.setVisibility(View.VISIBLE);
        } else if (isBooked) {
            holder.card.setCardBackgroundColor(0xFFFEF2F2);
            holder.card.setStrokeWidth(1);
            holder.card.setStrokeColor(android.content.res.ColorStateList.valueOf(0xFFFCA5A5));
            holder.tvDay.setTextColor(0xFFDC2626);
            holder.tvMonth.setTextColor(0xFFDC2626);
            holder.dateBlock.setCardBackgroundColor(0xFFFEE2E2);
            holder.tvTime.setTextColor(0xFF991B1B);
            holder.tvLabel.setTextColor(0xFFB91C1C);
            holder.selectIndicator.setCardBackgroundColor(0xFFFEE2E2);
            holder.ivCheck.setVisibility(View.GONE);
        } else {
            holder.card.setCardBackgroundColor(0xFFFFFFFF);
            holder.card.setStrokeWidth(1);
            holder.card.setStrokeColor(android.content.res.ColorStateList.valueOf(0xFFE2E8F0));
            holder.tvDay.setTextColor(0xFF6366F1);
            holder.tvMonth.setTextColor(0xFF6366F1);
            holder.dateBlock.setCardBackgroundColor(0xFFEEF2FF);
            holder.tvTime.setTextColor(0xFF1F2937);
            holder.tvLabel.setTextColor(0xFF6B7280);
            holder.selectIndicator.setCardBackgroundColor(0xFFF3F4F6);
            holder.ivCheck.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (isBooked) {
                return;
            }
            int prev = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(prev);
            notifyItemChanged(position);
            if (listener != null) listener.onScheduleSelected(s);
        });
    }

    @Override
    public int getItemCount() {
        return schedules.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final com.google.android.material.card.MaterialCardView card;
        final com.google.android.material.card.MaterialCardView dateBlock;
        final TextView tvDay, tvMonth, tvTime, tvLabel, tvFullBadge;
        final com.google.android.material.card.MaterialCardView selectIndicator;
        final ImageView ivCheck;

        Holder(@NonNull View itemView) {
            super(itemView);
            card = (com.google.android.material.card.MaterialCardView) itemView;
            dateBlock = itemView.findViewById(R.id.scheduleDateBlock);
            tvDay = itemView.findViewById(R.id.tvScheduleDay);
            tvMonth = itemView.findViewById(R.id.tvScheduleMonth);
            tvTime = itemView.findViewById(R.id.tvScheduleTime);
            tvLabel = itemView.findViewById(R.id.tvScheduleLabel);
            tvFullBadge = itemView.findViewById(R.id.tvFullBadge);
            selectIndicator = itemView.findViewById(R.id.scheduleSelectIndicator);
            ivCheck = itemView.findViewById(R.id.ivSelectCheck);
        }
    }
}
