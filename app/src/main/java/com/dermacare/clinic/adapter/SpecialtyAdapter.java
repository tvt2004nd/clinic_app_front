package com.dermacare.clinic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dermacare.clinic.R;

import java.util.List;

public class SpecialtyAdapter extends RecyclerView.Adapter<SpecialtyAdapter.Holder> {
    private final List<String> names;
    private final List<Integer> iconResIds;

    public SpecialtyAdapter(List<String> names, List<Integer> iconResIds) {
        this.names = names;
        this.iconResIds = iconResIds;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_specialty, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.imgIcon.setImageResource(iconResIds.get(position));
        holder.tvName.setText(names.get(position));
    }

    @Override
    public int getItemCount() {
        return names.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ImageView imgIcon;
        final TextView tvName;

        Holder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            tvName = itemView.findViewById(R.id.tvName);
        }
    }
}
