package com.dermacare.clinic.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dermacare.clinic.R;
import com.dermacare.clinic.adapter.ConversationAdapter;
import com.dermacare.clinic.data.api.ApiClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConversationListFragment extends Fragment {
    private static final String ARG_DOCTOR_MODE = "doctorMode";
    private final List<Map<String, Object>> conversations = new ArrayList<>();
    private ConversationAdapter adapter;
    private boolean doctorMode;
    private View tvEmpty;

    public static ConversationListFragment newInstance(boolean doctorMode) {
        ConversationListFragment fragment = new ConversationListFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_DOCTOR_MODE, doctorMode);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conversation_list, container, false);
        doctorMode = getArguments() != null && getArguments().getBoolean(ARG_DOCTOR_MODE);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ConversationAdapter(conversations, doctorMode, this::openChat);
        recyclerView.setAdapter(adapter);
        loadConversations();
        return view;
    }

    private void loadConversations() {
        ApiClient.getChatService(requireContext()).getConversations().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                conversations.clear();
                if (response.isSuccessful() && response.body() != null) {
                    conversations.addAll(response.body());
                }
                adapter.notifyDataSetChanged();
                tvEmpty.setVisibility(conversations.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Không tải được danh sách chat", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openChat(Map<String, Object> conversation) {
        Intent intent = new Intent(requireContext(), ChatActivity.class);
        intent.putExtra("conversationId", ((Number) conversation.get("conversationId")).longValue());
        intent.putExtra("chatTitle", String.valueOf(conversation.get(doctorMode ? "patientName" : "doctorName")));
        startActivity(intent);
    }
}
