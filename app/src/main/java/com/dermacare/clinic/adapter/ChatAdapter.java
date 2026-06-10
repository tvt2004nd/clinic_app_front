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

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    private List<Map<String, Object>> messages;
    private final Long currentUserId;

    public ChatAdapter(List<Map<String, Object>> messages, Long currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    public void addMessage(Map<String, Object> msg) {
        Object newId = msg.get("messageId");
        for (Map<String, Object> message : messages) {
            Object existingId = message.get("messageId");
            if (existingId != null && existingId.equals(newId)) {
                return;
            }
        }
        messages.add(msg);
        notifyItemInserted(messages.size() - 1);
    }

    public void setMessages(List<Map<String, Object>> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        Map<String, Object> msg = messages.get(position);
        Object senderIdObj = msg.get("senderId");
        Long senderId = null;
        if (senderIdObj instanceof Number) {
            senderId = ((Number) senderIdObj).longValue();
        } else if (senderIdObj instanceof String) {
            senderId = Long.parseLong((String) senderIdObj);
        }

        return senderId != null && senderId.equals(currentUserId) ? TYPE_SENT : TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_sent, parent, false);
            return new SentMessageHolder(view);
        }

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_received, parent, false);
        return new ReceivedMessageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Map<String, Object> msg = messages.get(position);
        String content = (String) msg.get("content");
        String timeStr = (String) msg.get("createdAt");
        String time = timeStr != null && timeStr.length() >= 16 ? timeStr.substring(11, 16) : timeStr != null ? timeStr : "";

        // ================= TIN NHẮN GỬI ĐI =================
        if (holder.getItemViewType() == TYPE_SENT) {
            SentMessageHolder sentHolder = (SentMessageHolder) holder;
            sentHolder.tvMessage.setText(content);
            sentHolder.tvTime.setText(time);
            return;
        }

        // ================= TIN NHẮN NHẬN ĐƯỢC =================
        ReceivedMessageHolder receivedHolder = (ReceivedMessageHolder) holder;
        receivedHolder.tvMessage.setText(content);
        receivedHolder.tvTime.setText(time);

        Object nameObj = msg.get("senderName");
        String name = nameObj != null ? String.valueOf(nameObj) : "";
        receivedHolder.tvSenderName.setText(name);

        // XỬ LÝ AVATAR (GLIDE)
        Object avatarObj = msg.get("avatarUrl");
        String avatarUrl = avatarObj != null ? String.valueOf(avatarObj) : "";

        if (avatarUrl != null && !avatarUrl.trim().isEmpty() && !avatarUrl.equals("null")) {
            // Có ảnh -> Ẩn Text chữ cái, Hiện ImageView
            receivedHolder.tvAvatarInitial.setVisibility(View.GONE);
            receivedHolder.ivAvatar.setVisibility(View.VISIBLE);

            String finalUrl = avatarUrl.startsWith("/") ? ApiClient.BASE_URL + avatarUrl.substring(1) : avatarUrl;

            Glide.with(receivedHolder.itemView.getContext())
                    .load(finalUrl)
                    .centerCrop()
                    .into(receivedHolder.ivAvatar);
        } else {
            // Không có ảnh -> Hiện Text chữ cái, Ẩn ImageView
            receivedHolder.tvAvatarInitial.setVisibility(View.VISIBLE);
            receivedHolder.ivAvatar.setVisibility(View.GONE);
            Glide.with(receivedHolder.itemView.getContext()).clear(receivedHolder.ivAvatar);

            String initial = !name.isEmpty() ? name.substring(0, 1).toUpperCase() : "?";
            receivedHolder.tvAvatarInitial.setText(initial);
        }
    }

    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }

    static class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;

        SentMessageHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }

    static class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime, tvSenderName, tvAvatarInitial;
        ImageView ivAvatar; // Ảnh thật

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
            tvAvatarInitial = itemView.findViewById(R.id.tvAvatarInitial);

            // Ánh xạ ImageView từ XML
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
        }
    }
}