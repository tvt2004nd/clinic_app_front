package com.dermacare.clinic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dermacare.clinic.R;
import com.dermacare.clinic.data.api.ApiClient;

import java.util.List;
import java.util.Map;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.Holder> {

    private final List<Map<String, Object>> items;
    private final boolean doctorMode;
    private final OnConversationClickListener listener;

    public interface OnConversationClickListener {
        void onConversationClick(Map<String, Object> conversation);
    }

    public ConversationAdapter(List<Map<String, Object>> items, boolean doctorMode, OnConversationClickListener listener) {
        this.items = items;
        this.doctorMode = doctorMode;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false); // Đảm bảo tên file XML đúng với tên file giao diện của bạn
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Map<String, Object> conv = items.get(position);

        // 1. Set Tên
        String nameObjKey = doctorMode ? "patientName" : "doctorName";
        String name = conv.get(nameObjKey) != null ? String.valueOf(conv.get(nameObjKey)) : "Người dùng";
        holder.tvName.setText(name);

        // 2. Set Tin nhắn cuối & Thời gian
        holder.tvLastMessage.setText(conv.get("lastMessage") != null ? String.valueOf(conv.get("lastMessage")) : "Bắt đầu trò chuyện");

        Object timeObj = conv.get("lastMessageTime");
        if (timeObj != null) {
            String timeStr = String.valueOf(timeObj);
            holder.tvTime.setText(timeStr.length() >= 16 ? timeStr.substring(11, 16) : timeStr);
        } else {
            holder.tvTime.setText("");
        }

        // 3. XỬ LÝ AVATAR BẰNG GLIDE
        String avatarObjKey = doctorMode ? "patientAvatar" : "doctorAvatar";
        Object avatarObj = conv.get(avatarObjKey);
        String avatarUrl = avatarObj != null ? String.valueOf(avatarObj) : "";

        if (avatarUrl != null && !avatarUrl.trim().isEmpty() && !avatarUrl.equals("null")) {
            // Có ảnh thật -> Hiện ImageView, Ẩn Text
            holder.tvAvatar.setVisibility(View.GONE);
            holder.ivAvatarReal.setVisibility(View.VISIBLE);

            String finalUrl = avatarUrl.startsWith("/") ? ApiClient.BASE_URL + avatarUrl.substring(1) : avatarUrl;

            Glide.with(holder.itemView.getContext())
                    .load(finalUrl)
                    .placeholder(R.drawable.ic_nav_profile)
                    .error(R.drawable.ic_nav_profile)
                    .centerCrop()
                    .into(holder.ivAvatarReal);
        } else {
            // Không có ảnh -> Ẩn ImageView, Hiện Text
            holder.tvAvatar.setVisibility(View.VISIBLE);
            holder.ivAvatarReal.setVisibility(View.GONE);
            Glide.with(holder.itemView.getContext()).clear(holder.ivAvatarReal);

            String initial = !name.isEmpty() ? name.substring(0, 1).toUpperCase() : "?";
            holder.tvAvatar.setText(initial);
        }

        // 4. Sự kiện Click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onConversationClick(conv);
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView tvName, tvTime, tvLastMessage, tvAvatar;
        final ImageView ivAvatarReal; // Ảnh thật

        Holder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            ivAvatarReal = itemView.findViewById(R.id.ivAvatarReal); // Khớp ID với file item_conversation.xml bạn gửi
        }
    }
}