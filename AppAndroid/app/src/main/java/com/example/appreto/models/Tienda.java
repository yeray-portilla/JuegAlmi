package com.example.appreto.models;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

// Una tienda tal como la devuelve GET info/tiendas
public class Tienda {
    @SerializedName("id")
    private int id;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("direccion")
    private String direccion;

    // Puede venir a null si la tienda todavia no tiene historia
    @SerializedName("historia")
    private String historia;

    @SerializedName("latitud")
    private double latitud;

    @SerializedName("longitud")
    private double longitud;

    // Lista de URLs de las fotos de la tienda
    @SerializedName("fotos")
    private List<String> fotos;

    // Departamentos que repara la tienda: [{id, nombre}] (Consolas, Moviles, Tablets)
    @SerializedName("departamentos")
    private List<OpcionCombo> departamentos;

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDireccion() { return direccion; }
    public String getHistoria() { return historia; }
    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }

    // Devuelve las fotos (lista vacia si la API no manda ninguna, asi nunca es null)
    public List<String> getFotos() {
        if (fotos == null) {
            return new ArrayList<>();
        }
        return fotos;
    }

    // Devuelve los departamentos (lista vacia si la API no manda ninguno)
    public List<OpcionCombo> getDepartamentos() {
        if (departamentos == null) {
            return new ArrayList<>();
        }
        return departamentos;
    }

    // Nombres de los departamentos separados por comas, ej. "Consolas, Moviles"
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
