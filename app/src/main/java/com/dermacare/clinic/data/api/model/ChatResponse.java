package com.dermacare.clinic.data.api.model;

import com.google.gson.annotations.SerializedName;

public class ChatResponse {
    @SerializedName("reply")
    private String reply;

    @SerializedName("intent")
    private String intent;

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }

    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }
}
