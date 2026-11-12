package com.example.appreto.models;

import com.google.gson.annotations.SerializedName;

public class SolicitudAdmin {
    @SerializedName("id")
    private int id;

    @SerializedName("fecha")
    private String fecha;

    @SerializedName("estado")
    private String estado;

    @SerializedName("usuario")
    private String usuario;

    @SerializedName("dispositivo")
    private String dispositivo;

    @SerializedName("tipo")
    private String tipo;

    @SerializedName("tienda")
    private String tienda;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("url_foto")
    private String urlFoto;

    @SerializedName("latitud")
    private double latitud;

    @SerializedName("longitud")
    private double longitud;

    @SerializedName("motivo_rechazo")
    private String motivoRechazo;

    @SerializedName("reparacion_id")
    private Integer reparacionId;

    @SerializedName("reparacion_estado")
    private String reparacionEstado;

    @SerializedName("precio_estimado")
    private Double precioEstimado;

    @SerializedName("precio_final")
    private Double precioFinal;

    public int getId() { return id; }
    public String getFecha() { return fecha; }
    public String getEstado() { return estado; }
    public String getUsuario() { return usuario; }
    public String getDispositivo() { return dispositivo; }
    public String getTipo() { return tipo; }
    public String getTienda() { return tienda; }
    public String getDescripcion() { return descripcion; }
    public String getUrlFoto() { return urlFoto; }
    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }
    public String getMotivoRechazo() { return motivoRechazo; }
    public Integer getReparacionId() { return reparacionId; }
    public String getReparacionEstado() { return reparacionEstado; }
    public Double getPrecioEstimado() { return precioEstimado; }
    public Double getPrecioFinal() { return precioFinal; }

    public boolean tieneUbicacion() {
        return latitud != 0 || longitud != 0;
    }

    public boolean estaCompletada() {
        return "completada".equals(reparacionEstado);
    }
}
