package com.example.appreto.models;

import com.google.gson.annotations.SerializedName;

public class OpcionCombo {
    @SerializedName("id")
    private int id;

    @SerializedName("nombre")
    private String nombre;

    public int getId() { return id; }
    public String getNombre() { return nombre; }

    @Override
    public String toString() {
        return nombre;
    }
}
