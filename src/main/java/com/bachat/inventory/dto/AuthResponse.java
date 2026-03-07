package com.bachat.inventory.dto;

import java.time.LocalDateTime;

public class AuthResponse {

    private String username;
    private String displayName;
    private String message;
    private LocalDateTime lastLogin;

    public AuthResponse() {}

    public AuthResponse(String username, String displayName, String message, LocalDateTime lastLogin) {
        this.username = username;
        this.displayName = displayName;
        this.message = message;
        this.lastLogin = lastLogin;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
}
