package com.example.appreto.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Tienda {
    @SerializedName("id")
    private int id;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("direccion")
    private String direccion;

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDireccion() { return direccion; }
    
    @Override
    public String toString() {
        return nombre + " · " + direccion;
    }
}
