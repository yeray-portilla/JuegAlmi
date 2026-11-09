package com.example.appreto.models;

import com.google.gson.annotations.SerializedName;

public class Aparato {
    @SerializedName("id")
    private int id;

    @SerializedName("marca_id")
    private int marcaId;

    @SerializedName("marca")
    private String marca;

    @SerializedName("departamento_id")
    private int departamentoId;

    @SerializedName("departamento")
    private String departamento;

    @SerializedName("modelo")
    private String modelo;

    public int getId() { return id; }
    public String getMarca() { return marca; }
    public String getDepartamento() { return departamento; }
    public String getModelo() { return modelo; }
}
