package com.dermacare.clinic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.dermacare.clinic.R;

import java.util.List;

public class CalendarDayAdapter extends RecyclerView.Adapter<CalendarDayAdapter.Holder> {
    public static final class Day {
        public final String name;
        public final String number;
        public final boolean selected;

        public Day(String name, String number, boolean selected) {
            this.name = name;
            this.number = number;
            this.selected = selected;
        }
    }

    private final List<Day> days;

    public CalendarDayAdapter(List<Day> days) {
        this.days = days;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Day day = days.get(position);
        holder.tvDayName.setText(day.name);
        holder.tvDayNumber.setText(day.number);

        int bgRes = day.selected
                ? R.drawable.bg_calendar_day_selected_modern
                : R.drawable.bg_calendar_day_default;
        holder.dayContainer.setBackgroundResource(bgRes);

        int nameColor = day.selected ? R.color.white : R.color.text_secondary;
        int numColor = day.selected ? R.color.white : R.color.text_primary;
        holder.tvDayName.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), nameColor));
        holder.tvDayNumber.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), numColor));
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final LinearLayout dayContainer;
        final TextView tvDayName;
        final TextView tvDayNumber;

        Holder(@NonNull View itemView) {
            super(itemView);
            dayContainer = itemView.findViewById(R.id.dayContainer);
            tvDayName = itemView.findViewById(R.id.tvDayName);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
        }
    }
}
