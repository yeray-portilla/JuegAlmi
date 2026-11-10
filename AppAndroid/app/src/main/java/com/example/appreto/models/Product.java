package com.example.appreto.models;

import com.google.gson.annotations.SerializedName;

// Un producto del catalogo: movil/tablet, videojuego o consola
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

    // Moviles/tablets: "telefono" o "tablet". En videojuegos y consolas lo pone MobilesFragment (setCategory).
    @SerializedName("tipo")
    private String tipo;

    // Moviles, tablets y consolas, ej. "128 GB" (los videojuegos no lo traen)
    @SerializedName("almacenamiento")
    private String almacenamiento;

    // Precios de alquiler (solo videojuegos)
    @SerializedName("precio_diario")
    private double precioDiario;

    @SerializedName("precio_semanal")
    private double precioSemanal;

    @SerializedName("precio_mensual")
    private double precioMensual;

    // Copias que quedan para alquilar (solo videojuegos). Integer para saber si la API no lo manda (null).
    @SerializedName("copias_alquiler")
    private Integer copiasAlquiler;

    // Nombre del producto (se llama getTitle() por el codigo que ya existia)
    public String getTitle() {
        return nombre;
    }

    // "Smartphones", "Tablets", "Videojuegos" o "Consolas"
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

    // true si queda stock para comprar
    public boolean hayStock() {
        return stock > 0;
    }

    // true si quedan copias para alquilar (si la API no lo dice, dejamos intentarlo y la API avisara)
    public boolean hayCopiasParaAlquilar() {
        return copiasAlquiler == null || copiasAlquiler > 0;
    }
}
