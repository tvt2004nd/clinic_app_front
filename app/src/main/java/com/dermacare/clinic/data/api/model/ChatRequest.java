package com.dermacare.clinic.data.api.model;

import com.google.gson.annotations.SerializedName;

public class ChatRequest {
    @SerializedName("message")
    private String message;

    @SerializedName("sessionUuid")
    private String sessionUuid;

    public ChatRequest(String message, String sessionUuid) {
        this.message = message;
        this.sessionUuid = sessionUuid;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSessionUuid() { return sessionUuid; }
    public void setSessionUuid(String sessionUuid) { this.sessionUuid = sessionUuid; }
}
