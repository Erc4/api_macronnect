package com.macronnect.api_macronnect.auth.dto;

public class LoginResponse {

    private String token;
    private String tipo = "Bearer";
    private String username;

    public LoginResponse(String token, String username) {
        this.token = token;
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public String getTipo() {
        return tipo;
    }

    public String getUsername() {
        return username;
    }
}