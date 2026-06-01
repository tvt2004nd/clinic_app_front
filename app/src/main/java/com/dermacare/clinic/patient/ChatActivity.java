package com.dermacare.clinic.patient;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dermacare.clinic.R;
import com.dermacare.clinic.adapter.ChatAdapter;
import com.dermacare.clinic.data.api.ApiClient;
import com.dermacare.clinic.data.api.ChatService;
import com.dermacare.clinic.data.api.model.ChatRequest;
import com.dermacare.clinic.data.api.model.ChatResponse;
import com.dermacare.clinic.model.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private EditText etMessage;
    private ImageButton btnSend;
    private ProgressBar progressBarChat;
    
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;
    private ChatService chatService;
    private String sessionUuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initViews();
        setupRecyclerView();
        
        chatService = ApiClient.getChatService(this);
        sessionUuid = UUID.randomUUID().toString(); // Generate unique session for chat

        btnSend.setOnClickListener(v -> sendMessage());
        
        // Initial bot greeting
        addBotMessage("Xin chào! Tôi là Trợ lý AI của DermaCare. Bạn cần tư vấn về vấn đề gì?");
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbarChat);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvChat = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        progressBarChat = findViewById(R.id.progressBarChat);
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvChat.setLayoutManager(layoutManager);
        rvChat.setAdapter(chatAdapter);
    }

    private void sendMessage() {
        String content = etMessage.getText().toString().trim();
        if (content.isEmpty()) return;

        // 1. Add User message to UI
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        chatAdapter.addMessage(new ChatMessage(content, false, time));
        rvChat.smoothScrollToPosition(messageList.size() - 1);
        etMessage.setText("");

        // 2. Call API
        progressBarChat.setVisibility(View.VISIBLE);
        ChatRequest request = new ChatRequest(content, sessionUuid);
        
        chatService.sendMessage(request).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                progressBarChat.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    addBotMessage(response.body().getReply());
                } else {
                    addBotMessage("Xin lỗi, hệ thống AI đang quá tải hoặc có lỗi xảy ra. Vui lòng thử lại sau.");
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                progressBarChat.setVisibility(View.GONE);
                addBotMessage("Lỗi kết nối mạng: " + t.getMessage());
            }
        });
    }

    private void addBotMessage(String text) {
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        chatAdapter.addMessage(new ChatMessage(text, true, time));
        rvChat.smoothScrollToPosition(messageList.size() - 1);
    }
}
