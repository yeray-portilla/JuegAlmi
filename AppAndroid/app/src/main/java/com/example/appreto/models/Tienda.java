package com.example.appreto.models;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class Tienda {
    @SerializedName("id")
    private int id;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("direccion")
    private String direccion;

    @SerializedName("historia")
    private String historia;

    @SerializedName("latitud")
    private double latitud;

    @SerializedName("longitud")
    private double longitud;

    @SerializedName("fotos")
    private List<String> fotos;

    @SerializedName("departamentos")
    private List<OpcionCombo> departamentos;

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDireccion() { return direccion; }
    public String getHistoria() { return historia; }
    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }

    public List<String> getFotos() {
        if (fotos == null) {
            return new ArrayList<>();
        }
        return fotos;
    }

    public List<OpcionCombo> getDepartamentos() {
        if (departamentos == null) {
            return new ArrayList<>();
        }
        return departamentos;
    }

    public String getNombresDepartamentos() {
        StringBuilder texto = new StringBuilder();
        for (OpcionCombo departamento : getDepartamentos()) {
            if (texto.length() > 0) {
                texto.append(", ");
            }
            texto.append(departamento.getNombre());
        }
        return texto.toString();
    }

    @Override
    public String toString() {
        return nombre + " · " + direccion;
    }
}
