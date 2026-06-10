package com.dermacare.clinic.model;

public class ChatMessage {
    private String content;
    private boolean isBot;
    private String timestamp;

    public ChatMessage(String content, boolean isBot, String timestamp) {
        this.content = content;
        this.isBot = isBot;
        this.timestamp = timestamp;
    }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isBot() { return isBot; }
    public void setBot(boolean bot) { isBot = bot; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
