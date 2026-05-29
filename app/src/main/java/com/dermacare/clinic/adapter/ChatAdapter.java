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

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    private List<Map<String, Object>> messages;
    private Long currentUserId;

    public ChatAdapter(List<Map<String, Object>> messages, Long currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    public void addMessage(Map<String, Object> msg) {
        Object newId = msg.get("messageId");
        for (Map<String, Object> m : messages) {
            if (m.get("messageId").equals(newId)) return;
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

        if (senderId != null && senderId.equals(currentUserId)) {
            return TYPE_SENT;
        } else {
            return TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_sent, parent, false);
            return new SentMessageHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_received, parent, false);
            return new ReceivedMessageHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Map<String, Object> msg = messages.get(position);
        String content = (String) msg.get("content");
        String timeStr = (String) msg.get("createdAt");
        String time = timeStr != null && timeStr.length() >= 16 ? timeStr.substring(11, 16) : timeStr != null ? timeStr : "";

        if (holder.getItemViewType() == TYPE_SENT) {
            SentMessageHolder h = (SentMessageHolder) holder;
            h.tvMessage.setText(content);
            h.tvTime.setText(time);
        } else {
            ReceivedMessageHolder h = (ReceivedMessageHolder) holder;
            h.tvMessage.setText(content);
            h.tvTime.setText(time);
            Object nameObj = msg.get("senderName");
            h.tvSenderName.setText(nameObj != null ? String.valueOf(nameObj) : "");
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
        TextView tvMessage, tvTime, tvSenderName;
        ReceivedMessageHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
        }
    }
}
