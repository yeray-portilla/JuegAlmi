package com.example.appreto.models;

import com.google.gson.annotations.SerializedName;

// Un aparato del usuario (GET soporte/dispositivos), ej. "Samsung Galaxy S21 (Moviles)"
public class Aparato {
    @SerializedName("id")
    private int id;

    @SerializedName("marca_id")
    private int marcaId;

    @SerializedName("marca")
    private String marca;

    // Departamento = tipo de aparato (Consolas, Moviles o Tablets). Sirve para saber que tiendas lo reparan.
    @SerializedName("departamento_id")
    private int departamentoId;

    @SerializedName("departamento")
    private String departamento;

    @SerializedName("modelo")
    private String modelo;

    public int getId() { return id; }
    public String getMarca() { return marca; }
    public int getDepartamentoId() { return departamentoId; }
    public String getDepartamento() { return departamento; }
    public String getModelo() { return modelo; }

    // "Samsung Galaxy S21"
    public String getNombre() {
        return marca + " " + modelo;
    }

    // Texto en el desplegable "Aparato": "Samsung Galaxy S21 (Moviles)"
    @Override
    public String toString() {
        return getNombre() + " (" + departamento + ")";
    }
}
