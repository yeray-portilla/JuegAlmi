package com.example.appreto.models;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("token")
    private String token;

    @SerializedName("usuario")
    private User user;

    public String getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }
}
