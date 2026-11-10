package com.tracker.dto;

import java.time.LocalDateTime;

public class ChatMessageDto {
    private String role; // "user" or "assistant"
    private String message;
    private String provider;
    private LocalDateTime timestamp;

    public ChatMessageDto() {}

    public ChatMessageDto(String role, String message, String provider) {
        this.role = role;
        this.message = message;
        this.provider = provider;
        this.timestamp = LocalDateTime.now();
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
