package com.dermacare.clinic.data.api;

import com.dermacare.clinic.data.api.model.ChatRequest;
import com.dermacare.clinic.data.api.model.ChatResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ChatService {
    @POST("api/chat/message")
    Call<ChatResponse> sendMessage(@Body ChatRequest request);
}
