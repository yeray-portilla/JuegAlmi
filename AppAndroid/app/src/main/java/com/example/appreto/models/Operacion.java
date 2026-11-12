package com.example.appreto.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Operacion {
    @SerializedName("tipo")
    private String tipo;

    @SerializedName("id")
    private int id;

    @SerializedName("fecha")
    private String fecha;

    @SerializedName("estado")
    private String estado;

    @SerializedName("total")
    private Double total;

    @SerializedName("detalle")
    private List<Detalle> detalle;

    @SerializedName("dispositivo")
    private String dispositivo;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("url_foto")
    private String urlFoto;

    @SerializedName("motivo_rechazo")
    private String motivoRechazo;

    public static class Detalle {
        @SerializedName("nombre")
        private String nombre;

        @SerializedName("cantidad")
        private Integer cantidad;

        @SerializedName("precio_unidad")
        private Double precioUnidad;

        public String getNombre() { return nombre; }
        public Integer getCantidad() { return cantidad; }
        public Double getPrecioUnidad() { return precioUnidad; }
    }

    public String getTipo() { return tipo; }
    public int getId() { return id; }
    public String getFecha() { return fecha; }
    public String getEstado() { return estado; }
    public Double getTotal() { return total; }
    public List<Detalle> getDetalle() { return detalle; }
    public String getDispositivo() { return dispositivo; }
    public String getDescripcion() { return descripcion; }
    public String getUrlFoto() { return urlFoto; }
    public String getMotivoRechazo() { return motivoRechazo; }
}
