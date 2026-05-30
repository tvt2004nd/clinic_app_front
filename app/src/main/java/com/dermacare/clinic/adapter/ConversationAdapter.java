package com.dermacare.clinic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dermacare.clinic.R;

import java.util.List;
import java.util.Map;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.Holder> {
    public interface OnConversationClick {
        void onClick(Map<String, Object> conversation);
    }

    private final List<Map<String, Object>> items;
    private final boolean doctorMode;
    private final OnConversationClick listener;

    public ConversationAdapter(List<Map<String, Object>> items, boolean doctorMode, OnConversationClick listener) {
        this.items = items;
        this.doctorMode = doctorMode;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Map<String, Object> item = items.get(position);
        String name = String.valueOf(item.get(doctorMode ? "patientName" : "doctorName"));
        holder.tvName.setText(name);

        String initials = "";
        if (name != null && !name.isEmpty()) {
            String[] parts = name.trim().split("\\s+");
            if (parts.length >= 2) {
                initials = String.valueOf(parts[0].charAt(0)) + parts[parts.length - 1].charAt(0);
            } else {
                initials = name.substring(0, Math.min(2, name.length())).toUpperCase();
            }
        }
        holder.tvAvatar.setText(initials.toUpperCase());

        Object lastMsg = item.get("lastMessage");
        if (lastMsg != null) {
            holder.tvLastMessage.setText(String.valueOf(lastMsg));
        } else {
            holder.tvLastMessage.setText("Chưa có tin nhắn");
        }

        Object lastTime = item.get("lastMessageTime");
        if (lastTime != null) {
            String timeStr = String.valueOf(lastTime);
            String time = timeStr.length() >= 16 ? timeStr.substring(11, 16) : timeStr;
            holder.tvTime.setText(time);
        } else {
            holder.tvTime.setText("");
        }

        holder.itemView.setOnClickListener(v -> listener.onClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView tvAvatar, tvName, tvTime, tvLastMessage;

        Holder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
        }
    }
}
