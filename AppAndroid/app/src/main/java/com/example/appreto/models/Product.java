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

    @SerializedName("almacenamiento")
    private String almacenamiento;

    @SerializedName("precio_diario")
    private double precioDiario;

    @SerializedName("precio_semanal")
    private double precioSemanal;

    @SerializedName("precio_mensual")
    private double precioMensual;

    @SerializedName("copias_alquiler")
    private Integer copiasAlquiler;

    public String getTitle() {
        return nombre;
    }

    public String getCategory() {
        if (tipo == null) {
            return "Producto";
        }
        if (tipo.equals("telefono")) {
            return "Smartphones";
        }
        if (tipo.equals("tablet")) {
            return "Tablets";
        }
        return tipo;
    }

    public void setCategory(String category) {
        this.tipo = category;
    }

    public int getId() { return id; }
    public String getMarca() { return marca; }
    public double getPrice() { return precio; }
    public int getStock() { return stock; }
    public String getFotoUrl() { return fotoUrl; }
    public String getAlmacenamiento() { return almacenamiento; }
    public double getPrecioDiario() { return precioDiario; }
    public double getPrecioSemanal() { return precioSemanal; }
    public double getPrecioMensual() { return precioMensual; }

    public boolean hayStock() {
        return stock > 0;
    }

    public boolean hayCopiasParaAlquilar() {
        return copiasAlquiler == null || copiasAlquiler > 0;
    }
}
