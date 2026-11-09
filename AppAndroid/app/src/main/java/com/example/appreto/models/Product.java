package com.example.appreto.models;

import com.google.gson.annotations.SerializedName;

public class Product {
    @SerializedName("id")
    private int id;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("marca")
    private String marca;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("precio")
    private double precio;

    @SerializedName("stock")
    private int stock;

    @SerializedName("foto_url")
    private String fotoUrl;

    @SerializedName("tipo")
    private String tipo;

    @SerializedName("precio_diario")
    private double precioDiario;

    // Para evitar que rompa lo actual, pondré "getTitle()" apuntando a "nombre"
    public String getTitle() {
        return nombre;
    }

    public String getCategory() {
        // En nuestro fragmento actual esto separaba móviles, tablets...
        // Podemos usar el campo 'tipo' (que en la API es 'telefono', 'tablet', etc.)
        if (tipo != null) {
            if (tipo.equals("telefono")) return "Smartphones";
            if (tipo.equals("tablet")) return "Tablets";
            return tipo;
        }
        return "Producto"; 
    }

    public double getPrice() {
        return precio;
    }

    public double getPrecioDiario() {
        return precioDiario;
    }

    public int getId() {
        return id;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setCategory(String category) {
        this.tipo = category;
    }
}
