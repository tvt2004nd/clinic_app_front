package com.dermacare.clinic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dermacare.clinic.R;


import java.util.ArrayList;
import java.util.List;

import android.content.Intent;


public class SimpleTextAdapter extends RecyclerView.Adapter<SimpleTextAdapter.Holder> {
    private List<String> items;
    private List<String> fullList;

    public SimpleTextAdapter(List<String> items) {
        this.items = new ArrayList<>(items);
        this.fullList = new ArrayList<>(items);
    }

    public void filter(String query) {
        items.clear();
        if (query.isEmpty()) {
            items.addAll(fullList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (String item : fullList) {
                if (item.toLowerCase().contains(lowerQuery)) {
                    items.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_simple_text, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.tvContent.setText(items.get(position));


    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView tvContent;

        Holder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tvContent);
        }
    }
}
