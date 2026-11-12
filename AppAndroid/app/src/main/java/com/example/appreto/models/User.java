package com.example.appreto.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class User {
    @SerializedName("id")
    private int id;

    @SerializedName("nombre_usuario")
    private String nombreUsuario;

    @SerializedName("email")
    private String email;

    @SerializedName("roles")
    private List<String> roles;

    public int getId() {
        return id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getRoles() {
        return roles;
    }
}
