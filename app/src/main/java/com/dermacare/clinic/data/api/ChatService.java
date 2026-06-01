package com.dermacare.clinic.data.api;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ChatService {
    @GET("/api/chat/conversations")
    Call<List<Map<String, Object>>> getConversations();

    @GET("/api/chat/conversation")
    Call<Map<String, Object>> getConversation(
            @Query("doctorId") Long doctorId,
            @Query("patientId") Long patientId
    );

    @GET("/api/chat/conversation/{conversationId}/messages")
    Call<List<Map<String, Object>>> getMessages(@Path("conversationId") Long conversationId);

    @POST("/api/chat/send")
    Call<Map<String, Object>> sendMessage(@Body Map<String, Object> body);
}
