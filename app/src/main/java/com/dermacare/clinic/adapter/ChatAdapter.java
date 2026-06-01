package com.dermacare.clinic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dermacare.clinic.R;
import com.dermacare.clinic.model.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatMessage> messageList;

    public ChatAdapter(List<ChatMessage> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);

        if (message.isBot()) {
            holder.layoutBotMessage.setVisibility(View.VISIBLE);
            holder.layoutUserMessage.setVisibility(View.GONE);
            holder.tvBotMessage.setText(message.getContent());
            holder.tvBotTime.setText(message.getTimestamp());
        } else {
            holder.layoutUserMessage.setVisibility(View.VISIBLE);
            holder.layoutBotMessage.setVisibility(View.GONE);
            holder.tvUserMessage.setText(message.getContent());
            holder.tvUserTime.setText(message.getTimestamp());
        }
    }

    @Override
    public int getItemCount() {
        return messageList != null ? messageList.size() : 0;
    }

    public void addMessage(ChatMessage message) {
        messageList.add(message);
        notifyItemInserted(messageList.size() - 1);
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutBotMessage, layoutUserMessage;
        TextView tvBotMessage, tvBotTime, tvUserMessage, tvUserTime;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutBotMessage = itemView.findViewById(R.id.layoutBotMessage);
            layoutUserMessage = itemView.findViewById(R.id.layoutUserMessage);
            tvBotMessage = itemView.findViewById(R.id.tvBotMessage);
            tvBotTime = itemView.findViewById(R.id.tvBotTime);
            tvUserMessage = itemView.findViewById(R.id.tvUserMessage);
            tvUserTime = itemView.findViewById(R.id.tvUserTime);
        }
    }
}
