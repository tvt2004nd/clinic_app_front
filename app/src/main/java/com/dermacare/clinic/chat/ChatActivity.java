package com.dermacare.clinic.chat;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dermacare.clinic.R;
import com.dermacare.clinic.adapter.ChatAdapter;
import com.dermacare.clinic.data.api.ApiClient;
import com.dermacare.clinic.util.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";
    private static final String WEBSOCKET_URL = "ws://10.0.2.2:8080/ws-raw";

    private RecyclerView rvMessages;
    private ChatAdapter chatAdapter;
    private EditText etMessage;
    private TextView tvChatTitle;
    
    private StompClient stompClient;
    private CompositeDisposable compositeDisposable;

    private Long conversationId;
    private Long currentUserId;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        SessionManager sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();
        token = sessionManager.getToken();

        // Get info from Intent
        // For example: doctorId, patientId, or conversationId directly
        conversationId = getIntent().getLongExtra("conversationId", -1L);
        String chatTitle = getIntent().getStringExtra("chatTitle");

        initViews(chatTitle);
        loadMessages();
        initStompClient();
    }

    private void initViews(String title) {
        tvChatTitle = findViewById(R.id.tvChatTitle);
        if (title != null) tvChatTitle.setText(title);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        rvMessages = findViewById(R.id.rvMessages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        
        chatAdapter = new ChatAdapter(new ArrayList<>(), currentUserId);
        rvMessages.setAdapter(chatAdapter);

        etMessage = findViewById(R.id.etMessage);
        findViewById(R.id.btnSend).setOnClickListener(v -> sendMessage());
    }

    private void initStompClient() {
        if (conversationId == -1L) {
            Toast.makeText(this, "Lỗi: Không tìm thấy cuộc hội thoại", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add headers (e.g. for JWT Auth)
        List<ua.naiksoftware.stomp.dto.StompHeader> headers = new ArrayList<>();
        if (token != null) {
            headers.add(new ua.naiksoftware.stomp.dto.StompHeader("Authorization", "Bearer " + token));
        }

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, WEBSOCKET_URL);
        compositeDisposable = new CompositeDisposable();

        Disposable dispLifecycle = stompClient.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(lifecycleEvent -> {
                    switch (lifecycleEvent.getType()) {
                        case OPENED:
                            Log.d(TAG, "Stomp connection opened");
                            break;
                        case ERROR:
                            Log.e(TAG, "Error", lifecycleEvent.getException());
                            break;
                        case CLOSED:
                            Log.d(TAG, "Stomp connection closed");
                            break;
                    }
                });

        compositeDisposable.add(dispLifecycle);

        // Subscribe to topic
        Disposable dispTopic = stompClient.topic("/topic/conversation/" + conversationId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(topicMessage -> {
                    Log.d(TAG, "Received: " + topicMessage.getPayload());
                    try {
                        JSONObject json = new JSONObject(topicMessage.getPayload());
                        Map<String, Object> msg = new HashMap<>();
                        msg.put("messageId", json.getLong("messageId"));
                        msg.put("senderId", json.getLong("senderId"));
                        msg.put("content", json.getString("content"));
                        msg.put("createdAt", json.getString("createdAt"));
                        
                        chatAdapter.addMessage(msg);
                        rvMessages.scrollToPosition(chatAdapter.getItemCount() - 1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, throwable -> {
                    Log.e(TAG, "Error on subscribe topic", throwable);
                });

        compositeDisposable.add(dispTopic);

        stompClient.connect(headers);
    }

    private void loadMessages() {
        if (conversationId == -1L) return;
        ApiClient.getChatService(this).getMessages(conversationId).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    chatAdapter.setMessages(response.body());
                    rvMessages.scrollToPosition(Math.max(chatAdapter.getItemCount() - 1, 0));
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(ChatActivity.this, "Không tải được tin nhắn", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        etMessage.setText("");

        Map<String, Object> localMsg = new HashMap<>();
        localMsg.put("messageId", System.currentTimeMillis());
        localMsg.put("senderId", currentUserId);
        localMsg.put("content", text);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        localMsg.put("createdAt", sdf.format(new java.util.Date()));
        chatAdapter.addMessage(localMsg);
        rvMessages.scrollToPosition(chatAdapter.getItemCount() - 1);

        Map<String, Object> body = new HashMap<>();
        body.put("conversationId", conversationId);
        body.put("senderId", currentUserId);
        body.put("content", text);

        ApiClient.getChatService(this).sendMessage(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Send failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(TAG, "Send error", t);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (stompClient != null) {
            stompClient.disconnect();
        }
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
        super.onDestroy();
    }
}
